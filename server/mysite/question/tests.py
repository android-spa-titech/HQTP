# -*- coding:utf-8 -*-
import doctest
import mysite.question.twutil.tw_util as tw_util
import mysite.question.views as views
import mysite.question.admin as admin
import mysite.question.test_views as test_views
import mysite.question.test_unit as test_unit


def load_tests(loader, tests, ignore):
    tests.addTests(test_unit.test_suite())
    tests.addTests(doctest.DocTestSuite(tw_util))
    tests.addTests(doctest.DocTestSuite(views))
    tests.addTests(doctest.DocTestSuite(admin))
    tests.addTests(loader.loadTestsFromModule(test_views))
    return tests
