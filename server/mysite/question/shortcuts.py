# -*- coding:utf-8 -*-

from mysite.question.twutil.consumer_info import spa_key, spa_secret
from django.test.client import Client
import json


def make_client():
    """
    >>> c = make_client()
    """

    c = Client(enforce_csrf_checks=True)
    return c


def access_auth_view(client, key=None, secret=None):
    # どちらもNoneかどちらもnot Noneでなければいけない
    error_msg = 'Usage: access_auth_view({KEY}, {SECRET})'
    assert not((key is None) ^ (secret is None)), error_msg

    # for convinient, key and secret are allowed blank
    # if blank then use android_spa's key and secret
    if key is None:  # secret is None, too.
        key = spa_key
        secret = spa_secret

    url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    url = url_template % (key, secret)
    response = client.get(url)
    jobj = json.loads(response.content)
    return jobj


def access_lecture_get_view(client):
    url = '/api/lecture/get/'
    response = client.get(url)
    jobj = json.loads(response.content)
    return jobj


def access_lecture_add_view(client, name, code):
    url = '/api/lecture/add/'
    response = client.post(url, dict(name=name, code=code))
    jobj = json.loads(response.content)
    return jobj


def access_timeline_get_view(client, id):
    url = '/api/lecture/timeline/?id=%s'
    response = client.get(url % id)
    jobj = json.loads(response.content)
    return jobj


def access_timeline_post_view(client, id, body,
                              before_virtual_ts=None, after_virtual_ts=None):
    url = '/api/lecture/timeline/'
    dic = dict(id=id, body=body)
    if before_virtual_ts is not None:
        dic['before_virtual_ts'] = before_virtual_ts
    if after_virtual_ts is not None:
        dic['after_virtual_ts'] = after_virtual_ts
    response = client.post(url, dic)
    jobj = json.loads(response.content)
    return jobj
