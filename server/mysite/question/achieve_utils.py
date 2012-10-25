# -*- coding: utf-8 -*-

from mysite.question.models import Achievement


def give_achievement(name, user):
    u"""
    実績とユーザーを指定し、ユーザーに実績を与える
    """

    achieve_dict = dict(first_login=100,
                        add_lecture=30,
                        one_post=1)

    Achievement.objects.create(name=name, point=achieve_dict[name],
                               achieved_by=user)
    user.total_point += achieve_dict[name]
    user.save()
