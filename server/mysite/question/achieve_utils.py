# -*- coding: utf-8 -*-

from mysite.question.models import Achievement
from django.db.models.aggregates import Max
import datetime


achieve_dict = dict(first_login=100,
                    add_lecture=30,
                    one_post=1,
                    upload_image=10,
                    consecutive_post=2,
                    )


def give_achievement(name, user):
    try:
        point = achieve_dict[name]
    except KeyError:
        print 'No such achievement'
        return

    Achievement.objects.create(name=name, point=point, achieved_by=user)


def is_post_5_minutes(lec_obj, comp_time):
    latest = lec_obj.post_set.aggregate(Max('posted_at'))['posted_at__max']
    return comp_time - latest < datetime.timedelta(seconds=300)
