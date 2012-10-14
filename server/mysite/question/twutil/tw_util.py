# -*- coding:utf-8 -*-

import tweepy
import consumer_info


PROFILE_IMAGE = ('http://api.twitter.com/1/users/profile_image'
                 + '?screen_name=%s&size=%s')
# なぜグローバル変数? save_img()以外でも使う予定があるのか?


def make_auth():
    return tweepy.OAuthHandler(consumer_info.key, consumer_info.secret)


def make_api(user_key, user_secret):
    auth = make_auth()
    auth.set_access_token(user_key, user_secret)
    return tweepy.API(auth_handler=auth)


def get_vc(user_key, user_secret):
    """
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
    else:
        return dict(id=vc.id, screen_name=vc.screen_name, name=vc.name)


def save_img(screen_name, size='bigger'):
    """
    get user icon by using twitter official API
    size is 'bigger'(73px), 'normal'(48px), 'mini'(24px), 'original'
    """

    url = PROFILE_IMAGE % (screen_name, size)

    # calc save directory (dirname == "HQTP/server/media/twicon")
    from django.conf import settings
    import os
    dirname = settings.MEDIA_ROOT

    import urllib
    f = urllib.urlopen(url)
    file_type = f.info().gettype()
    src = f.read()  # source string (binary)
    f.close()
    if 'image' in file_type:  # not error page
        out = open(os.path.join(dirname, screen_name), 'wb')
        # 'out' is local directory
        out.write(src)  # save icon image file from twitter server to local
        out.close()
        return url  # returns not local temporary file but icon URL
    else:  # error page HTML
        return None


def _test():
    import doctest
    doctest.testmod()


if __name__ == "__main__":
    _test()
