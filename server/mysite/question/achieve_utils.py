# -*- coding: utf-8 -*-

from mysite.question.models import Achievement
import datetime
import re
from mysite.question.specialwords import special_words


achieve_dict = dict(first_login=100,
                    add_lecture=30,
                    one_post=1,
                    upload_image=10,
                    consecutive_post=2,
                    post_inserted=10,
                    upload_url=2,
                    easter_egg=3,
                    attend_lecture=3,
                    )


def give_achievement(name, user):
    try:
        point = achieve_dict[name]
    except KeyError:
        print 'No such achievement'
        return

    Achievement.objects.create(name=name, point=point, achieved_by=user)


def is_post_5_minutes(lec_obj, user):
    posts_downwise = lec_obj.post_set.filter(added_by=user).order_by('-pk')
    if len(posts_downwise) == 1:  # user's first post to this lecture
        return False
    else:
        new_post = posts_downwise[0]
        prev_post = posts_downwise[1]
        return (new_post.posted_at - prev_post.posted_at
                < datetime.timedelta(minutes=5))


def contains_url(string):
    return bool(re.search(r'(https?|ftp)://[\w\-]+(\.).+', string))


def contains_specialwords(string):
    u"""
    特定の語を含んでいるか
    回数は考慮しない
    """
    for w in special_words:
        if w in string:
            return True
    return False


def first_or_interval(user):
    u"""
    userのattend_lecture実績が0である
    または，最後にattend_lecture実績をもらった日と日付が変わっている
    """
    if user.achievement_set.filter(name='attend_lecture').count() == 0:
        return True
    else:
        last_attend = user.achievement_set.filter(
            name='attend_lecture').order_by('-pk')[0].created_at.date()
        delta = datetime.date.today() - last_attend
        return delta.days >= 1
