# -*- coding:utf-8 -*-
import doctest
import mysite.question.twutil.tw_util as tw_util
import mysite.question.views as views
import mysite.question.admin as admin
import mysite.question.test_views as test_views


def load_tests(loader, tests, ignore):
    tests.addTests(doctest.DocTestSuite(tw_util))
    tests.addTests(doctest.DocTestSuite(views))
    tests.addTests(doctest.DocTestSuite(admin))
    tests.addTests(loader.loadTestsFromModule(test_views))
    return tests
