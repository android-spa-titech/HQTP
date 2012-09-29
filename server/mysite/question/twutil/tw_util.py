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


def place_holder_func():
    """
    この関数はget_vcが置き換えられることに伴い、
    このファイルのdocstringが0個になるのを防ぐために必要
    """


def get_vc(user_key, user_secret):
    """
    this docstring can't call in test
    because replace this with get_vc_mock

    >>> import consumer_info as ci
    >>> vc = get_vc(ci.spa_key,ci.spa_secret)
    >>> vc['id'] == ci.spa_id
    True
    >>> vc['screen_name'] == ci.spa_screen_name
    True
    >>> vc['name'] == ci.spa_name
    True
    >>> vc = get_vc('dummy key','dummy secret')
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


def save_img(screen_name, size='bigger'):
    """
    get user icon by using twitter official API
    size is 'bigger'(73px), 'normal'(48px), 'mini'(24px), 'original'
    """
    url = ('http://api.twitter.com/1/users/profile_image'
           '?screen_name=%s&size=%s'
           % (screen_name, size))

    # calc save directory (dirname = HQTP/server/media/twicon)
    import os
    dirname = os.path.join(
        os.path.dirname(os.getcwd()), 'media', 'twicon')

    import urllib
    f = urllib.urlopen(url)
    src = f.read()
    f.close()
    if src.find('<!DOCTYPE html>') != 0:
        # image file (not error page)
        out = open(os.path.join(dirname, screen_name), 'wb')
        out.write(src)
        out.close()
        return url  # 暫定的に保存したファイルではなく取得元のリンクを返す
    else:
        return None


def _test():
    import doctest
    doctest.testmod()

if __name__ == "__main__":
    _test()
