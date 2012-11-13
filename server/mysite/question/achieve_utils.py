# -*- coding: utf-8 -*-

from mysite.question.models import Achievement
from django.db.models.aggregates import Max
import datetime
import re


achieve_dict = dict(first_login=100,
                    add_lecture=30,
                    one_post=1,
                    upload_image=10,
                    consecutive_post=2,
                    post_inserted=10,
                    upload_url=2,
                    )


def give_achievement(name, user):
    try:
        point = achieve_dict[name]
    except KeyError:
        print 'No such achievement'
        return

    Achievement.objects.create(name=name, point=point, achieved_by=user)


def latest_post_time(lec_obj):
    return lec_obj.post_set.aggregate(Max('posted_at'))['posted_at__max']


def is_post_5_minutes(g_time, l_time):
    if l_time is None:
        return False
    return g_time - l_time < datetime.timedelta(seconds=300)


def contains_url(string):
    return bool(re.search(r'(https?|ftp)://[\w\-]+(\.).+', string))
