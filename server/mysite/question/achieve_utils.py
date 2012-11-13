# -*- coding: utf-8 -*-

from mysite.question.models import Achievement
import re


achieve_dict = dict(first_login=100,
                    add_lecture=30,
                    one_post=1,
                    upload_image=10,
                    post_inserted=10,
                    upload_url=2,
                    easter_egg=3,
                    )


def give_achievement(name, user):
    try:
        point = achieve_dict[name]
    except KeyError:
        print 'No such achievement'
        return

    Achievement.objects.create(name=name, point=point, achieved_by=user)


def contains_url(string):
    return bool(re.search(r'(https?|ftp)://[\w\-]+(\.).+', string))


special_words = {
    u'遅刻した',
    u'眠い',
    u'♨',
    }


def contains_specialwords(string):
    u"""
    特定の語を含んでいるか
    回数は考慮しない
    """
    for w in special_words:
        if w in string:
            return True
    return False
