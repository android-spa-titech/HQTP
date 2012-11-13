# -*- coding:utf-8 -*-

# Django imports
from django.http import (HttpResponse,
                         HttpResponseNotFound,
                         HttpResponseForbidden,
                         HttpResponseBadRequest,
                         HttpResponseServerError)
from django.contrib.auth.models import User
from django.contrib.auth import authenticate, login
from django.utils.datastructures import MultiValueDictKeyError

# Python module imports
import json
import os
import doctest
from time import time

# mysite imports
from mysite.question.models import (Post,
                                    user_to_dict,
                                    Lecture)
import mysite.question.twutil.tw_util as tw_util
import mysite.question.image_utils as image_utils
from mysite.question.achieve_utils import (give_achievement,
                                           contains_url,
                                           contains_specialwords)
from django.db.models.aggregates import Sum


def convert_context_to_json(context):
    "Convert the context dictionary into a JSON object"
    # Note: This is *EXTREMELY* naive; in reality, you'll need
    # to do much more complex handling to ensure that arbitrary
    # objects -- such as Django model instances or querysets
    # -- can be serialized as JSON.
    return json.dumps(context)


def json_response(context={}):
    context['status'] = 'OK'
    return HttpResponse(convert_context_to_json(context),
                        mimetype='application/json')


def json_response_not_found(context={}):
    context['status'] = 'Not Found'
    return HttpResponseNotFound(convert_context_to_json(context),
                                mimetype='application/json')


def json_response_forbidden(context={}):
    context['status'] = 'Forbidden'
    return HttpResponseForbidden(convert_context_to_json(context),
                                 mimetype='application/json')


def json_response_bad_request(context={}):
    context['status'] = 'Bad Request'
    return HttpResponseBadRequest(convert_context_to_json(context),
                                  mimetype='application/json')


def json_response_server_error(context={}):
    context['status'] = 'Server Error'
    return HttpResponseServerError(convert_context_to_json(context),
                                   mimetype='application/json')


def auth_view(request):
    try:
        key = request.GET['access_token_key']
        secret = request.GET['access_token_secret']
    except MultiValueDictKeyError:
        # bad request
        return json_response_bad_request()

    try:
        # get twitter account by key and secret
        tw_account = tw_util.get_vc(key, secret)
    except TypeError:
        # Error reason is not well known
        # sending dummy access token key/secret causes error
        return json_response_server_error()

    if tw_account == {}:
        # 正しいアクセストークンキー、シークレットでなかった場合 など
        return json_response_not_found()
    user_name = tw_account['id']

    # get twitter icon URL and save icon image to local
    # 暫定的に認証時に毎回アイコンを取得
    twicon = image_utils.get_img(tw_account['icon_url'])
    if twicon is None:
        relative_pathname = 'default_twicon'
    else:
        relative_pathname = os.path.join('twicon', str(user_name))
        absolute_pathname = image_utils.build_media_absolute_pathname(
            relative_pathname)
        image_utils.save_bindata(absolute_pathname, twicon)
    icon_url = image_utils.build_media_absolute_url(request, relative_pathname)

    # 新規に作成されたユーザーも、登録済みだったユーザーも
    # どちらもパスワードとしてtemp_passwordを設定する
    temp_password = User.objects.make_random_password()
    try:
        # HQTP user exists
        user = User.objects.get(username=user_name)
        user.set_password(temp_password)
        user.save()
        created = False
    except User.DoesNotExist:
        # User has twitter account, but doesn't have HQTP account
        # create new user
        user = User.objects.create_user(username=user_name,
                                        email='',
                                        password=temp_password)
        profile = user.get_profile()
        profile.screen_name = tw_account['screen_name']
        profile.name = tw_account['name']
        profile.icon_url = icon_url
        profile.save()
        give_achievement('first_login', user)
        created = True

    auth_user = authenticate(username=user_name, password=temp_password)
    # セキュリティのために、パスワード認証ができないようにします
    # アクセストークンKEY、SECRETによる認証しか行いません
    auth_user.set_unusable_password()
    if auth_user.is_active:
        # Log in successful
        login(request, auth_user)
        user_info = user_to_dict(auth_user)
    else:
        # User is deleted
        return json_response_not_found()

    return json_response(context=dict(created=created, user=user_info))


def lecture_get_view(request):
    if not request.user.is_authenticated():
        # get need authentication
        return json_response_forbidden()

    lecs = [lec.to_dict() for lec in Lecture.objects.all()]
    return json_response(context=dict(lectures=lecs))


def lecture_add_view(request):
    try:
        code = request.POST['code']
        name = request.POST['name']
    except MultiValueDictKeyError:
        return json_response_bad_request()

    if not request.user.is_authenticated():
        # add need authentication
        return json_response_forbidden()

    # get_or_create(): 新規作成したらcreated = True
    lec, created = Lecture.objects.get_or_create(
        code=code, defaults=dict(name=name))
    if created:
        give_achievement('add_lecture', request.user)
    return json_response(context=dict(created=created, lecture=lec.to_dict()))


def lecture_timeline_view(request):
    if request.method == 'GET':
        try:
            # get timeline
            lecture_id = int(request.GET['id'])
        except (MultiValueDictKeyError, ValueError):
            # key 'id' is not requested
            # or id is not integer
            return json_response_bad_request()

        if 'since_id' in request.GET:
            try:
                since_id = int(request.GET['since_id'])
            except ValueError:
                return json_response_bad_request()
        else:
            since_id = 0

        if not request.user.is_authenticated():
            # get need authentication
            return json_response_forbidden()

        try:
            lec = Lecture.objects.get(pk=lecture_id)
        except Lecture.DoesNotExist:
            # invalid lecture ID
            return json_response_not_found()

        # successfully get timeline
        posts = [q.to_dict() for q in
                 lec.post_set.filter(pk__gt=since_id).order_by('virtual_ts')]
        return json_response(context=dict(posts=posts))

    elif request.method == 'POST':
        try:
            lecture_id = int(request.POST['id'])
        except (MultiValueDictKeyError, ValueError):
            return json_response_bad_request()

        # boolean flags
        use_before_vts = ('before_virtual_ts' in request.POST)
        use_after_vts = ('after_virtual_ts' in request.POST)
        use_text = ('body' in request.POST)
        use_image = ('image' in request.FILES)

        if use_before_vts != use_after_vts:
            # only one is requested and the other one is not
            # NOTE: != is logical exclusive-or
            return json_response_bad_request()

        if use_text == use_image:
            # bodyとimageのどちらか一方を指定
            return json_response_bad_request()

        if use_before_vts and use_after_vts:
            # post to between 2 posts
            try:
                before_vts = int(request.POST['before_virtual_ts'])
                after_vts = int(request.POST['after_virtual_ts'])
            except ValueError:
                return json_response_bad_request()
            vts = Post.calc_mid(before_vts, after_vts)
        else:
            # post to latest
            vts = Post.time_to_vts(time())

        if not request.user.is_authenticated():
            # get need authentication
            return json_response_forbidden()

        try:
            lec = Lecture.objects.get(pk=lecture_id)
        except Lecture.DoesNotExist:
            # invalid lecture ID
            return json_response_not_found()

        post = lec.post_set.create(added_by=request.user,
                                   virtual_ts=vts)
        if use_text:
            post.body = request.POST['body']
            post.save()
            if contains_url(request.POST['body']):
                give_achievement('upload_url', request.user)
            if contains_specialwords(request.POST['body']):
                give_achievement('easter_egg', request.user)
        elif use_image:
            # save image file
            # ユニークなfilenameとして、Postのpkを使う
            image = request.FILES['image']
            filename = 'img_' + str(post.pk)
            relative_pathname = os.path.join('uploads', filename)
            absolute_pathname = image_utils.build_media_absolute_pathname(
                relative_pathname)
            image_utils.save_bindata(absolute_pathname, image.read())
            image_url = image_utils.build_media_absolute_url(
                request, relative_pathname)
            post.image_url = image_url
            post.save()
            give_achievement('upload_image', request.user)

        give_achievement('one_post', request.user)

        if use_before_vts:
            for p in Post.objects.filter(
                virtual_ts=before_vts).exclude(added_by=request.user):
                give_achievement('post_inserted', p.added_by)
        return json_response(context=dict(post=post.to_dict()))


def user_view(request):
    # ユーザー情報取得API
    if request.method == 'GET':
        try:
            user_id = int(request.GET['id'])
        except (MultiValueDictKeyError, ValueError):
            return json_response_bad_request()

        if not request.user.is_authenticated():
            return json_response_forbidden()

        try:
            user = User.objects.get(pk=user_id)
        except User.DoesNotExist:
            # ID が不正だったら Not Found
            return json_response_not_found()

        user_info = user_to_dict(user)
        return json_response(context=dict(user=user_info))


def achievement_view(request):
    u"実績一覧取得API"

    if request.method == 'GET':
        try:
            user_id = int(request.GET['id'])
        except (MultiValueDictKeyError, ValueError):
            return json_response_bad_request()

        if 'since_id' in request.GET:
            try:
                since_id = int(request.GET['since_id'])
            except ValueError:
                return json_response_bad_request()
        else:
            since_id = 0

        if not request.user.is_authenticated():
            return json_response_forbidden()

        try:
            user = User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return json_response_not_found()

        # Get the total point
        context = user.achievement_set.aggregate(total_point=Sum('point'))

        achievements = [a.to_dict() for a in
                        user.achievement_set.filter(pk__gt=since_id)]
        context['achievements'] = achievements
        return json_response(context)


def _test():
    doctest.testmod()
