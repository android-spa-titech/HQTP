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

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    >>> c = Client(enforce_csrf_checks=True)
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'

    # create user first time
    >>> url = url_template % (spa_key, spa_secret)
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
    True

    # user is crated only first time
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['created']
    False

    # if send dummy access token, return Not Found
    >>> url = url_template % ('dummy key', 'dummy secret')
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Not Found'
    True

    # some dummy access token, cause server error
    # catch exception and return server error
    >>> url = url_template % ('spam', 'egg')
    >>> c = Client(enforce_csrf_checks=True)
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
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

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    >>> c = Client(enforce_csrf_checks=True)

    # getはauthしていないとできません（Forbidden）
    >>> response1 = c.get('/api/get/')
    >>> jobj1 = json.loads(response1.content)
    >>> jobj1['status'] == 'Forbidden'
    True

    # authをします
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> response2 = c.get(url)
    >>> jobj2 = json.loads(response2.content)
    >>> jobj2['status'] == 'OK'
    True

    # authして始めてgetできます
    >>> response3 = c.get('/api/get/')
    >>> jobj3 = json.loads(response3.content)
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

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    >>> c = Client(enforce_csrf_checks=True)

    # postはauthしていないとできません（Forbidden）
    >>> response = c.post('/api/post/',
    ...                   dict(title='before login', body='cannot post'))
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Forbidden'
    True

    # authをします
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True

    # authして始めてpostできます
    >>> response = c.post('/api/post/',
    ...                   dict(title='after auth',body='can post'))
    >>> jobj=json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True

    # getで確かめてみると、確かに投稿が反映されています。
    >>> response = c.get('/api/get/')
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True
    >>> jobj['posts'] == [dict(title='after auth', body='can post')]
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

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    # 認証をします
    >>> c = Client(enforce_csrf_checks=True)
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> r = c.get(url)
    >>> j = json.loads(r.content)
    >>> j['status'] == 'OK'
    True

    # すると同時にログインも出来ているので、postも可能です
    >>> r = c.post('/api/post/', dict(title='test', body='hello world'))
    >>> r = c.get('/api/get/')
    >>> j = json.loads(r.content)
    >>> j['status'] == 'OK'
    True
    >>> j['posts'] == [dict(title='test', body='hello world')]
    True
    """


def test_about_bad_request():
    """
    >>> from django.test.client import Client
    >>> import json
    >>> from question.twutil.consumer_info import spa_key, spa_secret

    >>> c = Client(enforce_csrf_checks=True)

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
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> response = c.get(url)

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

    >>> from django.contrib.auth.models import User
    >>> from question.twutil.consumer_info import spa_key, spa_secret
    >>> from django.test.client import Client
    >>> import json

    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)

    >>> c = Client(enforce_csrf_checks=True)
    >>> response = c.get(url)

    >>> jobj = json.loads(response.content)
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
