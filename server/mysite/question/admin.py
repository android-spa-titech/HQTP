# -*- coding:utf-8 -*-

from django.contrib import admin
from django.contrib.auth.models import User
from django.contrib.auth.admin import UserAdmin
from mysite.question.models import Post, Lecture, Achievement, UserProfile


class PostAdmin(admin.ModelAdmin):
    search_fields = ['body', 'added_by']
    date_hierarchy = 'posted_at'

admin.site.register(Post, PostAdmin)


class LectureAdmin(admin.ModelAdmin):
    search_fields = ['name', 'code']

admin.site.register(Lecture, LectureAdmin)


class AchievementAdmin(admin.ModelAdmin):
    search_fields = ['name']
    date_hierarchy = 'created_at'

admin.site.register(Achievement, AchievementAdmin)


class UserProfileInline(admin.StackedInline):
    model = UserProfile
    extra = 0


class MyUserAdmin(UserAdmin):
    inlines = UserAdmin.inlines + [UserProfileInline]

admin.site.unregister(User)
admin.site.register(User, MyUserAdmin)


def dummytest():
    """
    >>> 1==1
    True
    """


def _test():
    import doctest
    doctest.testmod()
