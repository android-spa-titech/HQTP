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


def get_vc(user_key, user_secret):
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


def _test():
    import doctest
    doctest.testmod()

if __name__ == "__main__":
    _test()
