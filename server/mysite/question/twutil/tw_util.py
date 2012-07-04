# -*- coding:utf-8 -*-

import tweepy
import consumer_info


def make_auth():
    return tweepy.OAuthHandler(consumer_info.key, consumer_info.secret)


def make_api(user_key, user_secret):
    auth = make_auth()
    auth.set_access_token(user_key, user_secret)
    api = tweepy.API(auth_handler=auth)
    return api


def _get_vc(user_key, user_secret):
    """
    >>> import consumer_info as ci
    >>> vc=_get_vc(ci.spa_key,ci.spa_secret)
    >>> vc['id'] == ci.spa_id
    True
    >>> vc['screen_name'] == ci.spa_screen_name
    True
    >>> vc['name'] == ci.spa_name
    True
    >>> vc=_get_vc('dummy key','dummy secret')
    >>> vc=={}
    True
    """
    api = make_api(user_key, user_secret)
    vc = api.verify_credentials()
    if not vc:
        # 正しいアクセストークンキー、シークレットでなかった場合
        return {}
    ret = dict(id=vc.id, screen_name=vc.screen_name, name=vc.name)
    return ret


def get_vc_mock(user_key, user_secret):
    """
    Tweepy mock-up

    If you want to use more twitter users,
    please add user into twitter_database.

    this method has 3 type behaviors
    1. If input key is in the database, and secret is correct
       then return user information

    >>> import consumer_info as ci
    >>> vc = get_vc(ci.spa_key, ci.spa_secret)
    >>> vc == dict(id=ci.spa_id, screen_name=ci.spa_screen_name,
    ...            name=ci.spa_name)
    True

    2. If input key is in the database, but secret is wrong
       then return empty dictionary (and server return not found)

    >>> vc = get_vc(ci.spa_key, 'egg')
    >>> vc == {}
    True

    3. If input key isn't in the database
       then return TypeError (and server return server error)

    >>> vc = get_vc('spam', 'egg')
    Traceback (most recent call last):
        ...
    TypeError: character mapping must return integer, None or unicode

    """
    import consumer_info as ci
    twitter_database = {
        ci.spa_key: dict(secret=ci.spa_secret,
                      vc=dict(id=ci.spa_id,
                              screen_name=ci.spa_screen_name,
                              name=ci.spa_name)
                      )
        }
    not_found_keys = set(['dummy key'])

    if user_key in twitter_database:
        user_info = twitter_database[user_key]
        if user_secret == user_info['secret']:
            return user_info['vc']
        else:
            return {}
    elif user_key in not_found_keys:
        return {}
    else:
        raise TypeError(('character mapping must return integer, '
                         'None or unicode'))


def get_vc(user_key, user_secret):
    u"""
    "Regardless of the value of the DEBUG setting in mysite/settings.py,
     all Django tests run with DEBUG=False."

    Djangoの公式ドキュメントに書いてあるこの条件を利用して、
    DEBUGの値がFalseならtestの実行中と判断し、
    DEBUGの値がTrueならrunserverまたはshellの実行中と判定している
    mysite/settings.pyのDEBUGの値はFalseにしないでください
    """
    from django.conf.global_settings import DEBUG
    if DEBUG:
        return _get_vc(user_key, user_secret)
    else:
        return get_vc_mock(user_key, user_secret)


def _test():
    import doctest
    doctest.testmod()

if __name__ == "__main__":
    _test()
