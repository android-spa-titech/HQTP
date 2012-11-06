# -*- coding:utf-8 -*-

from mysite.question.twutil.consumer_info import spa_key, spa_secret
import json


def access_template(method, api_name, **kwargs):
    url = '/api/' + api_name + '/'
    response = method(url, kwargs)
    return json.loads(response.content)


def access_auth_view(client, **key_and_secret):
    if key_and_secret == {}:
        return access_template(client.get, 'auth', access_token_key=spa_key,
                               access_token_secret=spa_secret)
    else:
        return access_template(client.get, 'auth', **key_and_secret)


def access_lecture_get_view(client):
    return access_template(client.get, 'lecture/get')


def access_lecture_add_view(client, name, code):
    return access_template(client.post, 'lecture/add', name=name, code=code)


def access_timeline_get_view(client, lecture_id, **since_id):
    return access_template(client.get, 'lecture/timeline',
                           id=lecture_id, **since_id)


def access_timeline_post_view(client, lecture_id,
                              body=None, image=None, **vts):
    if (body is not None) == (image is not None):
        assert 'Usage: \'body\' or \'image\' must be needed.'
    elif body is not None:
        return access_template(client.post, 'lecture/timeline', id=lecture_id,
                               body=body, **vts)
    elif image is not None:
        return access_template(client.post, 'lecture/timeline', id=lecture_id,
                               image=image, **vts)


def access_user_get_view(client, user_id):
    return access_template(client.get, 'user', id=user_id)


def access_achievement_get_view(client, user_id, **since_id):
    return access_template(client.get, 'user/achievement',
                           id=user_id, **since_id)
