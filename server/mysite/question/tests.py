# -*- coding:utf-8 -*-
import doctest
import mysite.question.twutil.tw_util as tw_util
import mysite.question.views as views
import mysite.question.admin as admin
import mysite.question.test_views as test_views
import mysite.question.test_unit as test_unit


def load_tests(loader, tests, ignore):
    # unit test
    tests.addTests(test_unit.auth_test_case())
    tests.addTests(test_unit.lecture_test_case())
    tests.addTests(test_unit.timeline_test_case())

    # doc test
    tests.addTests(doctest.DocTestSuite(tw_util))
    tests.addTests(doctest.DocTestSuite(views))
    tests.addTests(doctest.DocTestSuite(admin))
    tests.addTests(loader.loadTestsFromModule(test_views))
    return tests
