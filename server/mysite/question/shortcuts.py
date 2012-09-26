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
    """
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> 'created' in jobj
    True
    >>> user = jobj['user']
    >>> 'id' in user
    True
    >>> 'name' in user
    True
    >>> 'icon_url' in user
    True
    """
    # for convinient, key and secret are allowed blank
    # if blank then use android_spa's key and secret
    if key is None and secret is None:
        key = spa_key
        secret = spa_secret
    elif key is None or secret is None:
        raise Exception('Usage: access_auth_view({KEY}, {SECRET})')

    url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    url = url_template % (key, secret)
    response = client.get(url)
    jobj = json.loads(response.content)
    return jobj


def access_lecture_get_view(client):
    """
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_lecture_get_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> 'lectures' in jobj
    True
    """
    url = '/api/lecture/get/'
    response = client.get(url)
    jojb = json.loads(response.content)
    return jojb


def access_lecture_add_view(client, name, code):
    """
    >>> name = 'Programming 1'
    >>> code = '0B123456789'
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_lecture_add_view(c, name=name, code=code)
    >>> jobj['status'] == 'OK'
    True
    >>> 'created' in jobj
    True

    >>> lecture = jobj['lecture']
    >>> lecture['name'] == name
    True
    >>> lecture['code'] == code
    True
    >>> 'id' in lecture
    True
    """
    url = '/api/lecture/add/'
    response = client.post(url, dict(name=name, code=code))
    jobj = json.loads(response.content)
    return jobj


def access_timeline_get_view(client, id):
    """
    This test will be added
    """
    u"""
    >>> name = 'Arch1'
    >>> code = 't001'

    # 下準備（授業の作成）
    >>> c0 = make_client()
    >>> jobj0a = access_auth_view(c0)
    >>> jobj0b = access_lecture_add_view(c, name=name, code=code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_timeline_get_view(c, id=lecture_id)
    >>> jobj['status'] == 'OK'
    True
    >>> 'posts' in jojb
    True
    """
    url = '/api/lecture/timeline/?id=%s'
    response = client.get(url % id)
    jojb = json.loads(response.content)
    return jojb


def access_timeline_post_view(client, id, body,
                              before_virtual_ts=None, after_virtual_ts=None):
    """
    This test will be added
    """
    u"""
    >>> name = 'Arch1'
    >>> code = 't001'
    >>> body = u'MIPSとは'

    # 下準備（授業の作成）
    >>> c0 = make_client()
    >>> jobj0a = access_auth_view(c0)
    >>> jobj0b = access_lecture_add_view(c, name=name, code=code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_timeline_post_view(c, id=lecture_id, body=body,
    ...                                  before_virtual_ts=1000,
    ...                                  after_virtual_ts=2000)
    >>> jobj['status'] == 'OK'
    True
    >>> post = jojb['post']
    >>> 'id' in post
    True

    # 追加先授業の情報は正しいか
    >>> lecture = post['lecture']
    >>> lecture['id'] == lecture_id
    True
    >>> lecture['name'] == name
    True
    >>> lecture['code'] == code
    True

    # 投稿内容の情報は正しいか
    >>> post['body'] == body
    True

    # 投稿ユーザーの情報は正しいか
    >>> user = post['user']
    >>> 'id' in user and 'name' in user and 'icon_url' in user
    True

    >>> 'time' in post
    True
    >>> 'virtual_ts' in post
    True
    """
    # check parameter
    cnd = (before_virtual_ts is None and after_virtual_ts is None
           or before_virtual_ts is not None and after_virtual_ts is not None)
    msg = 'Bad Request'
    assert cnd, msg

    url = '/api/lecture/timeline/'
    if before_virtual_ts is None:  # after_virtual_ts is None too.
        response = client.post(url, dict(id=id, body=body))
    else:
        response = client.post(url, dict(id=id, body=body,
                                         before_virtual_ts=before_virtual_ts,
                                         after_virtual_ts=after_virtual_ts))
    jobj = json.loads(response.content)
    return jobj
