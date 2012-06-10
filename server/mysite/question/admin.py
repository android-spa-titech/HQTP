# -*- coding:utf-8 -*-

from django.contrib import admin
from mysite.question.models import Question


class QuestionAdmin(admin.ModelAdmin):
    search_fields = ['title', 'body', 'added_by']
    date_hierarchy = 'posted_at'

admin.site.register(Question, QuestionAdmin)


def dummytest():
    """
    >>> 1==1
    True
    """


def _test():
    import doctest
    doctest.testmod()
