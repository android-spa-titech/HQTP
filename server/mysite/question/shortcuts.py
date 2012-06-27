from mysite.question.twutil.consumer_info import spa_key, spa_secret
from django.test.client import Client
import json
import datetime


def make_client():
    """
    >>> from mysite.question.shortcuts import make_client
    >>> c = make_client()
    """

    c = Client(enforce_csrf_checks=True)
    return c


def access_auth_view(client, key=None, secret=None):
    """
    >>> from mysite.question.tests import clean_users
    >>> clean_users()

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
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


def access_get_view(client):
    """
    >>> from mysite.question.tests import clean_questions
    >>> clean_questions()

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_get_view)
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_get_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['posts'] == []
    True
    """

    url = '/api/get/'
    response = client.get(url)
    jojb = json.loads(response.content)
    return jojb


def access_post_view(client, title=None, body=None):
    """
    >>> from mysite.question.tests import clean_questions
    >>> clean_questions()

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_get_view,
    ...                                        access_post_view)
    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj = access_post_view(c, 'Hello', 'World')
    >>> jobj['status'] == 'OK'
    True
    >>> jobj = access_get_view(c)
    >>> jobj['posts'][0]['title'] == 'Hello'
    True
    >>> jobj['posts'][0]['body'] == 'World'
    True
    """

    # for convenient, title and body are allowed blank
    # to make unique title and body, use now date and time
    now = str(datetime.datetime.now())
    if title is None:
        title = 'TITLE:' + now
    if body is None:
        body = 'BODY:' + now

    url = '/api/post/'
    response = client.post(url, dict(title=title, body=body))
    jobj = json.loads(response.content)
    return jobj
