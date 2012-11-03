# -*- coding: utf-8 -*-

from mysite.question.models import Achievement


achieve_dict = dict(first_login=100,
                    add_lecture=30,
                    one_post=1,
                    )


def give_achievement(name, user):
    try:
        point = achieve_dict[name]
    except KeyError:
        print 'No such achievement'
        return

    Achievement.objects.create(name=name, point=point, achieved_by=user)
