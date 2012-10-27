# -*- coding:utf-8 -*-

from mysite.question.twutil.consumer_info import spa_key, spa_secret
from django.test.client import Client
import json


def make_client():
    """>>> c = make_client()"""
    return Client()


def access_template(method, api_name, **kwargs):
    u"""
    他のショートカット関数を生成するためのひな形
    Bad Requestについてテストする時以外は、これを直接呼び出すようなことはしない
    """
    url = '/api/' + api_name + '/'
    response = method(url, kwargs)
    return json.loads(response.content)


def access_auth_view(client, **key_and_secret):
    if key_and_secret == {}:
        # for convenient, key and secret are allowed blank
        # if blank then use android_spa's key and secret
        return access_template(client.get, 'auth', access_token_key=spa_key,
                               access_token_secret=spa_secret)
    else:
        return access_template(client.get, 'auth', **key_and_secret)


def access_lecture_get_view(client):
    return access_template(client.get, 'lecture/get')


def access_lecture_add_view(client, name, code):
    return access_template(client.post, 'lecture/add', name=name, code=code)


def access_timeline_get_view(client, lecture_id):
    return access_template(client.get, 'lecture/timeline', id=lecture_id)


def access_timeline_post_view(client, lecture_id, body, **vts):
    return access_template(client.post, 'lecture/timeline',
                           id=lecture_id, body=body, **vts)


def access_user_get_view(client, user_id):
    return access_template(client.get, 'user', id=user_id)
