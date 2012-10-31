# -*- coding:utf-8 -*-

from mysite.question.twutil.consumer_info import spa_key, spa_secret
from django.test.client import Client
import json


def make_client():
    """
    >>> c = make_client()
    """

    return Client(enforce_csrf_checks=True)


def access_auth_view(client, key=None, secret=None):
    # どちらもNoneかどちらもnot Noneでなければいけない
    error_msg = 'Usage: access_auth_view({KEY}, {SECRET})'
    assert not((key is None) != (secret is None)), error_msg

    # for convenient, key and secret are allowed blank
    # if blank then use android_spa's key and secret
    if key is None and secret is None:
        key = spa_key
        secret = spa_secret

    url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    url = url_template % (key, secret)
    response = client.get(url)
    return json.loads(response.content)


def access_lecture_get_view(client):
    url = '/api/lecture/get/'
    response = client.get(url)
    return json.loads(response.content)


def access_lecture_add_view(client, name, code):
    url = '/api/lecture/add/'
    response = client.post(url, dict(name=name, code=code))
    return json.loads(response.content)


def access_timeline_get_view(client, lecture_id):
    url = '/api/lecture/timeline/?id=%s'
    response = client.get(url % lecture_id)
    return json.loads(response.content)


def access_timeline_post_view(client, lecture_id, body=None,
                              before_virtual_ts=None, after_virtual_ts=None,
                              image=None):
    url = '/api/lecture/timeline/'
    dic = dict(id=lecture_id)
    if body is not None:
        dic['body'] = body
    if before_virtual_ts is not None:
        dic['before_virtual_ts'] = before_virtual_ts
    if after_virtual_ts is not None:
        dic['after_virtual_ts'] = after_virtual_ts
    if image is not None:
        dic['image'] = image
    response = client.post(url, dic)
    return json.loads(response.content)


def access_user_get_view(client, user_id):
    url = '/api/user/?id=%d'
    response = client.get(url % user_id)
    return json.loads(response.content)
