# -*- coding:utf-8 -*-


def clean_questions():
    """ before test, call this method for clear questions """
    from mysite.question.models import Question
    Question.objects.all().delete()


def clean_users():
    """ before test, call this method for clear users """
    from django.contrib.auth.models import User
    User.objects.all().delete()


def test_about_auth_view():
    """
    >>> clean_users()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)


    >>> c = make_client()

    # create user first time
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
    True

    # user is crated only first time
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
    False

    # test about user identifier (ID)
    >>> 'user' in jobj
    True
    >>> 'name' in jobj['user']
    True
    >>> 'id' in jobj['user']
    True

    # if send dummy access token, return Not Found
    >>> jobj = access_auth_view(c, key='dummy key', secret='dummy secret')
    >>> jobj['status'] == 'Not Found'
    True

    # some dummy access token, cause server error
    # catch exception and return server error
    >>> jobj = access_auth_view(c, key='spam', secret='egg')
    >>> jobj['status'] == 'Server Error'
    True
    """

    pass


def test_about_get_view():
    """
    # getが認証しないとできないことを確かめるテスト
    # authする前にgetをするとForbiddenが返る
    # authした後にgetをするとOKが返る

    >>> clean_questions()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_get_view)


    >>> c = make_client()

    # getはauthしていないとできません（Forbidden）
    >>> jobj1 = access_get_view(c)
    >>> jobj1['status'] == 'Forbidden'
    True

    # authをします
    >>> jobj2 = access_auth_view(c)
    >>> jobj2['status'] == 'OK'
    True

    # authして始めてgetできます
    >>> jobj3 = access_get_view(c)
    >>> jobj3['status'] == 'OK'
    True
    >>> 'posts' in jobj3
    True
    """


def test_about_post():
    """
    # postが認証しないとできないことを確かめるテスト
    # authする前にpostをするとForbiddenが返る
    # authした後にpostをするとOKが返る

    >>> clean_questions()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_get_view,
    ...                                        access_post_view)


    >>> c = make_client()

    # postはauthしていないとできません（Forbidden）
    >>> jobj = access_post_view(c, title='before login', body='cannot post')
    >>> jobj['status'] == 'Forbidden'
    True

    # authをします
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True

    # authして始めてpostできます
    >>> jobj = access_post_view(c, title='after auth', body='can post')
    >>> jobj['status'] == 'OK'
    True

    # 戻り値の検証
    >>> post = jobj['post']
    >>> post['title'] == 'after auth'
    True
    >>> post['body'] == 'can post'
    True
    >>> 'user' in post
    True
    >>> 'id' in post['user']
    True
    >>> 'name' in post['user']
    True
    >>> 'time' in post
    True

    # getで確かめてみると、確かに投稿が反映されています。
    >>> jobj = access_get_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> post = jobj['posts'][0]
    >>> 'id' in post
    True
    >>> post['title'] == 'after auth'
    True
    >>> post['body'] == 'can post'
    True
    >>> 'user' in post
    True
    >>> 'id' in post['user']
    True
    >>> 'name' in post['user']
    True
    >>> 'time' in post
    True
    """
    pass


def test_about_csrf():
    """
    >>> clean_questions()

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)

    # don't use csrf checking
    >>> c1 = Client()
    >>> response = c1.get(url)
    >>> response = c1.post('/api/post/',
    ...                    dict(title='csrf test2',body='this pass'))
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True

    # use csrf checking
    >>> c2 = Client(enforce_csrf_checks=True)
    >>> response = c2.get(url)
    >>> response = c2.post('/api/post/',
    ...                    dict(title='csrf test2',body='this pass'))
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True
    """

    # もしviews.pyのpost_viewに@csrf_exemptデコレーターをつけないと、
    # c2を使う2つ目のテストは失敗する（response.status_codeが200ではなく403となるので）
    # 一方c1を使う1つ目のテストは成功する。
    #なので、テストクライアントを作るときはenforce_csrf_checks=Trueを指定する

    pass


def test_about_login():
    """
    >>> clean_questions()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_get_view,
    ...                                        access_post_view)


    # 認証をします
    >>> c = make_client()
    >>> j = access_auth_view(c)
    >>> j['status'] == 'OK'
    True

    # すると同時にログインも出来ているので、postも可能です
    >>> j = access_post_view(c, title='test', body='hello world')
    >>> j['status'] == 'OK'
    True

    # getで確かめてみると、確かに投稿が反映されています。
    >>> j = access_get_view(c)
    >>> j['status'] == 'OK'
    True
    >>> post = j['posts'][0]
    >>> post['title'] == 'test'
    True
    >>> post['body'] == 'hello world'
    True
    """


def test_about_bad_request():
    """
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)


    >>> import json
    >>> from question.twutil.consumer_info import spa_key, spa_secret

    >>> c = make_client()

    # this is bad request.
    # because does not send access_token_key,access_token_secret
    # so server return Bad Request
    >>> url_template = '/api/auth/?access_token=%s'
    >>> url = url_template % 'ACC'
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Bad Request'
    True

    # to test bad request of post, login
    >>> jobj = access_auth_view(c)

    # this is bad request.
    # because does not send body
    # so server return Bad Request
    >>> response = c.post('/api/post/',
    ...                   dict(title='test none boddy'))
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Bad Request'
    True

    # this is bad request.
    # because does not send title
    # so server return Bad Request
    >>> response = c.post('/api/post/',
    ...                   dict(body='test none title'))
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Bad Request'
    True
    """
    # Must send essential parameter
    # if does't send, server return Bad Request


def test_about_user_profile():
    """
    >>> clean_users()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)

    >>> from django.contrib.auth.models import User


    >>> c = make_client()
    >>> jobj = access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
    True

    # get android_spa user username=twitter id
    >>> usr = User.objects.get(username='580619600')

    # get UserProfile and check screen_name,name
    >>> profile = usr.get_profile()
    >>> profile.screen_name == 'android_spa'
    True
    >>> profile.name == 'android_spa'
    True
    """
    # UserProfileが正しく機能しているか確認


from django.test import TestCase
import doctest
import mysite.question.twutil.tw_util as tw_util
import mysite.question.views as views
import mysite.question.admin as admin
import mysite.question.shortcuts as shortcuts


def load_tests(loader, tests, ignore):
    tests.addTests(doctest.DocTestSuite(tw_util))
    tests.addTests(doctest.DocTestSuite(views))
    tests.addTests(doctest.DocTestSuite(admin))
    tests.addTests(doctest.DocTestSuite(shortcuts))
    return tests


class SimpleTest(TestCase):
    def test_basic_addition(self):
        """
        Tests that 1 + 1 always equals 2.
        """
        self.assertEqual(1 + 1, 2)
