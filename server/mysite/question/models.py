# -*- coding:utf-8 -*-

from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save


class Lecture(models.Model):
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=255)  # 授業コードは数値以外もありうる

    def __unicode__(self):
        return u'[%s] %s' % (self.code, self.name)

    def to_dict(self):
        return dict(id=self.pk,
                    name=self.name,
                    code=self.code)


class Question(models.Model):
    body = models.TextField()
    added_by = models.ForeignKey(User, null=True, blank=True)
    posted_at = models.DateTimeField(auto_now_add=True)
    lecture = models.ForeignKey(Lecture, null=True, blank=True)

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
        >>> from time import time
        >>> t = time()
        >>> vts = Question.time_to_vts(t)
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


class UserProfile(models.Model):
    user = models.OneToOneField(User)

    # Additional information about User
    screen_name = models.CharField(max_length=255)
    name = models.CharField(max_length=255)
    icon_url = models.CharField(max_length=255)


def user_to_dict(user):
    return dict(id=user.pk,
                name=user.get_profile().screen_name,
                icon_url=user.get_profile().icon_url)


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
