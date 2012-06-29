# -*- coding:utf-8 -*-

from django.http import (HttpResponse,
                         HttpResponseNotFound,
                         HttpResponseForbidden,
                         HttpResponseBadRequest,
                         HttpResponseServerError)
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.models import User
from django.contrib.auth import authenticate, login
from django.utils.datastructures import MultiValueDictKeyError
import json
from mysite.question.models import Question, user_to_dict


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
    from twutil.tw_util import get_vc

    try:
        key = request.GET['access_token_key']
        secret = request.GET['access_token_secret']
    except MultiValueDictKeyError:
        # bad request
        return json_response_bad_request()

    try:
        vc = get_vc(key, secret)
    except TypeError:
        # Error reason is not well known
        # sending dummy access token key/secret causes error
        return json_response_server_error()

    if vc == {}:
        return json_response_not_found()
    user_name = vc['id']

    # 新規に作成されたユーザーも、登録済みだったユーザーも
    # どちらもパスワードとしてtemp_passwordを設定する
    temp_password = User.objects.make_random_password()
    try:
        user = User.objects.get(username=user_name)
        user.set_password(temp_password)
        user.save()
        created = False
    except User.DoesNotExist:
        # create new user
        user = User.objects.create_user(user_name, '', temp_password)
        profile = user.get_profile()
        profile.screen_name = vc['screen_name']
        profile.name = vc['name']
        profile.save()
        created = True

    auth_user = authenticate(username=user_name, password=temp_password)
    # セキュリティのために、パスワード認証ができないようにします
    # アクセストークンKEY、SECRETによる認証しか行いません
    auth_user.set_unusable_password()
    if auth_user is not None:
        if auth_user.is_active:
            # Log in successful
            login(request, auth_user)
            user_info = user_to_dict(auth_user)
        else:
            # User is deleted
            return json_response_not_found()
    else:
        # 新規作成・パスワードの設定を行っているのでここにはこないはず
        return json_response_server_error()

    context = dict(created=created, user=user_info)
    return json_response(context)


def get_view(request):
    if not request.user.is_authenticated():
        # get need auth
        return json_response_forbidden()

    posts = [q.to_dict() for q in Question.objects.all()]
    context = dict(
       posts=posts
    )
    return json_response(context)


@csrf_exempt
def post_view(request):
    try:
        title = request.POST['title']
        body = request.POST['body']
    except MultiValueDictKeyError:
        # bad request
        return json_response_bad_request()

    added_by = request.user
    if added_by.is_authenticated():
        q = Question.objects.create(title=title, body=body, added_by=added_by)
        return json_response(context=dict(post=q.to_dict()))
    else:
        return json_response_forbidden()


def _test():
    import doctest
    doctest.testmod()
