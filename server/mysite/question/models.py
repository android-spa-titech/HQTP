# -*- coding:utf-8 -*-

from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.db.models.aggregates import Sum


class Lecture(models.Model):
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=255)  # 授業コードは数値以外もありうる

    def __unicode__(self):
        return u'[%s] %s' % (self.code, self.name)

    def to_dict(self):
        return dict(id=self.pk, name=self.name, code=self.code)


class Post(models.Model):
    body = models.TextField()
    added_by = models.ForeignKey(User)
    posted_at = models.DateTimeField(auto_now_add=True)
    lecture = models.ForeignKey(Lecture)

    # 64bit(from -9223372036854775808 to 9223372036854775807)
    virtual_ts = models.BigIntegerField()

    def __unicode__(self):
        return self.body

    def to_dict(self):
        return dict(id=self.pk,
                    body=self.body,
                    user=user_to_dict(self.added_by),
                    time=self.posted_at.isoformat(),
                    lecture=self.lecture.to_dict(),
                    virtual_ts=self.virtual_ts)

    @classmethod
    def time_to_vts(cls, t):
        """
        # how to use this method
        >>> from time import time
        >>> t = time()
        >>> vts = Post.time_to_vts(t)

        # test this method
        >>> import time
        >>> from datetime import date
        >>> d = date(2012, 9, 30)
        >>> t = time.mktime(d.timetuple())
        >>> vts = Post.time_to_vts(t)
        >>> vts
        134893080000000000L
        """
        s = repr(t)  # 精度を保ったまま文字列にする
        splits = s.split('.')  # 小数点で分割(小数点以下は6桁)
        vts = int(splits[0]) * (10 ** 6) + int(splits[1])

        # 仮想タイムスタンプの重複ができるだけ起きないように
        # BigIntegerの範囲でできるだけ大きくする
        vts *= 100
        return vts

    @classmethod
    def calc_mid(cls, vts1, vts2):
        return (vts1 + vts2) / 2


class Achievement(models.Model):
    name = models.CharField(max_length=255)
    created_at = models.DateTimeField(auto_now_add=True)
    point = models.IntegerField()
    achieved_by = models.ForeignKey(User)

    def to_dict(self):
        return dict(id=self.pk,
                    name=self.name,
                    point=self.point,
                    created_at=self.created_at.isoformat())


class UserProfile(models.Model):
    user = models.OneToOneField(User)

    # Additional information about User
    screen_name = models.CharField(max_length=255)
    name = models.CharField(max_length=255)
    icon_url = models.CharField(max_length=255)


def user_to_dict(user):
    point = user.achievement_set.aggregate(Sum('point'))['point__sum']
    return dict(id=user.pk,
                name=user.get_profile().screen_name,
                icon_url=user.get_profile().icon_url,
                total_point=point)


def create_user_profile(sender, instance, created, **kwargs):
    """
    if user is created (or changed),
    then create user profile about the user, using signal

    kwargs contains such key 'raw', 'signal', 'using'
    """

    if created:
        # user is created, not changed
        UserProfile.objects.create(user=instance)

post_save.connect(create_user_profile, sender=User)
