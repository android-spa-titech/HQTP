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

    >>> c=Client(enforce_csrf_checks=True)
    >>> url_template='/api/auth/?access_token_key=%s&access_token_secret=%s'

    # create user first time
    >>> url=url_template % (spa_key, spa_secret)
    >>> response=c.get(url)
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True
    >>> jobj['created']
    True

    # user is crated only first time
    >>> response=c.get(url)
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True
    >>> jobj['created']
    False

    # if send dummy access token, return Not Found
    >>> url = url_template % ('dummy key', 'dummy secret')
    >>> response=c.get(url)
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='Not Found'
    True
    """

    pass


def test_about_get_view():
    """
    >>> clean_questions()

    >>> from django.test.client import Client
    >>> import json

    >>> c=Client(enforce_csrf_checks=True)
    >>> response=c.get('/api/get/')

    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True
    >>> jobj['posts']==[]
    True
    """
    pass


def test_about_post():
    """
    # postが認証しないとできないことを確かめるテスト
    # authする前にpostをするとForbiddenが返る
    # authした後にpostをするとOKが返る

    >>> clean_questions()

    >>> from django.test.client import Client
    >>> from mysite.question.twutil.consumer_info import spa_key, spa_secret
    >>> import json

    >>> c=Client(enforce_csrf_checks=True)

    # getは認証の必要がありません
    >>> response=c.get('/api/get/')
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True
    >>> jobj['posts']==[]
    True

    # postはauthしていないとできません（Forbidden）
    >>> response=c.post('/api/post/',
    ...                 dict(title='before login', body='cannot post'))
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='Forbidden'
    True

    # authをします
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> response = c.get(url)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'OK'
    True

    # authして始めてpostできます
    >>> response=c.post('/api/post/',
    ...                 dict(title='after auth',body='can post'))
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True

    # getで確かめてみると、確かに投稿が反映されています。
    >>> response=c.get('/api/get/')
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True
    >>> jobj['posts']==[dict(title='after auth', body='can post')]
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
    >>> c1=Client()
    >>> response=c1.get(url)
    >>> response=c1.post('/api/post/',
    ...                  dict(title='csrf test2',body='this pass'))
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
    True

    # use csrf checking
    >>> c2=Client(enforce_csrf_checks=True)
    >>> response=c2.get(url)
    >>> response=c2.post('/api/post/',
    ...                  dict(title='csrf test2',body='this pass'))
    >>> jobj=json.loads(response.content)
    >>> jobj['status']=='OK'
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
    >>> c = Client(enforce_csrf_checks=True)
    >>> url_template = '/api/auth/?access_token_key=%s&access_token_secret=%s'
    >>> url = url_template % (spa_key, spa_secret)
    >>> r = c.get(url)
    >>> j = json.loads(r.content)
    >>> j['status'] == 'OK'
    True
    >>> r = c.post('/api/post/', dict(title='test', body='hello world'))
    >>> r = c.get('/api/get/')
    >>> j = json.loads(r.content)
    >>> j['status'] == 'OK'
    True
    >>> j['posts'] == [dict(title='test', body='hello world')]
    True
    """
    # ログインと認証が同時にできるようになりました


from django.test import TestCase
import doctest
import mysite.question.twutil.tw_util as tw_util
import mysite.question.views as views
import mysite.question.admin as admin


def load_tests(loader, tests, ignore):
    tests.addTests(doctest.DocTestSuite(tw_util))
    tests.addTests(doctest.DocTestSuite(views))
    tests.addTests(doctest.DocTestSuite(admin))
    return tests


class SimpleTest(TestCase):
    def test_basic_addition(self):
        """
        Tests that 1 + 1 always equals 2.
        """
        self.assertEqual(1 + 1, 2)
