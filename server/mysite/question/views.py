# -*- coding:utf-8 -*-

from django.http import (HttpResponse,
                         HttpResponseNotFound,
                         HttpResponseForbidden,
                         HttpResponseBadRequest,
                         HttpResponseServerError)
from django.contrib.auth.models import User
from django.contrib.auth import authenticate, login
from django.utils.datastructures import MultiValueDictKeyError
import json
from mysite.question.models import (Post,
                                    user_to_dict,
                                    Lecture)
import mysite.question.twutil.tw_util as tw_util
import mysite.question.image_utils as image_utils
from time import time
import os


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
    return json_response(context=dict(created=created, lecture=lec.to_dict()))


def lecture_timeline_view(request):
    if request.method == 'GET':
        try:
            # get timeline
            lecture_id = request.GET['id']
        except MultiValueDictKeyError:
            # key 'id' is not requested
            return json_response_bad_request()

        if not request.user.is_authenticated():
            # get need authentication
            return json_response_forbidden()

        try:
            lec = Lecture.objects.get(pk=lecture_id)
        except Lecture.DoesNotExist:
            # invalid lecture ID
            return json_response_not_found()
        else:
            # successfully get timeline
            posts = [q.to_dict()
                     for q in lec.post_set.order_by('virtual_ts')]
            return json_response(context=dict(posts=posts))

    elif request.method == 'POST':
        try:
            lecture_id = request.POST['id']
        except MultiValueDictKeyError:
            return json_response_bad_request()

        if (('body' in request.POST)
            == ('image' in request.FILES)):
            # bodyとimageのどちらか一方を指定
            return json_response_bad_request()

        # boolean flags
        use_before_vts = ('before_virtual_ts' in request.POST)
        use_after_vts = ('after_virtual_ts' in request.POST)

        if use_before_vts != use_after_vts:
            # only one is requested and the other one is not
            # NOTE: != is logical exclusive-or
            return json_response_bad_request()

        if not request.user.is_authenticated():
            # get need authentication
            return json_response_forbidden()

        try:
            lec = Lecture.objects.get(pk=lecture_id)
        except Lecture.DoesNotExist:
            # invalid lecture ID
            return json_response_not_found()

        if use_before_vts and use_after_vts:
            # post to between 2 posts
            vts = Post.calc_mid(int(request.POST['before_virtual_ts']),
                                int(request.POST['after_virtual_ts']))
        else:
            # post to latest
            vts = Post.time_to_vts(time())

        post = lec.post_set.create(added_by=request.user,
                                   virtual_ts=vts)
        if 'body' in request.POST:
            post.body = request.POST['body']
            post.save()
        else:  # 'image' in request.FILES:
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

        return json_response(context=dict(post=post.to_dict()))


def user_view(request):
    # ユーザー情報取得API
    if request.method == 'GET':
        try:
            user_id = request.GET['id']
        except MultiValueDictKeyError:
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


def _test():
    import doctest
    doctest.testmod()
