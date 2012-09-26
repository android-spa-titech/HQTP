# -*- coding:utf-8 -*-


def clean_questions():
    """ before test, call this method for clear questions """
    from mysite.question.models import Question
    Question.objects.all().delete()


def clean_users():
    """ before test, call this method for clear users """
    from django.contrib.auth.models import User
    User.objects.all().delete()


def clean_lectures():
    """ before test, call this method for clear lectures """
    from mysite.question.models import Lecture
    Lecture.objects.all().delete()


###############################################################################
# /api/auth/に関するテスト
###############################################################################
def test_about_auth():
    u"""
    >>> clean_users()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)


    >>> c = make_client()

    # create user first time
    >>> jobj1 = access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True
    >>> jobj1['created']
    True
    >>> user = jobj1['user']
    >>> 'id' in user
    True
    >>> 'name' in user
    True
    >>> 'icon_url' in user
    True


    # user is crated only first time
    >>> c2 = make_client()
    >>> jobj2 = access_auth_view(c2)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['created']
    False
    """


def test_about_auth__badrequest():
    u"""
    access_token_key, access_token_secretが両方渡されないとBad Requestを返す

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)


    >>> import json
    >>> from question.twutil.consumer_info import spa_key, spa_secret

    >>> c = make_client()

    >>> url1 = '/api/auth/?access_token_key=KEY'
    >>> response = c.get(url1)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Bad Request'
    True

    >>> url2 = '/api/auth/?access_token_secret=SECRET'
    >>> response = c.get(url2)
    >>> jobj = json.loads(response.content)
    >>> jobj['status'] == 'Bad Request'
    True
    """


def teset_about_auth__notfound():
    u"""
    TwitterのOAuthの正しくないkey, secretの場合Not Foundを返す

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)

    >>> c = make_client()
    >>> jobj = access_auth_view(c, key='dummy key', secret='dummy secret')
    >>> jobj['status'] == 'Not Found'
    True
    """


def test_about_auth__servererror():
    u"""
    key, sercretによってはServer Errorを返す

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view)

    >>> c = make_client()
    >>> jobj = access_auth_view(c, key='spam', secret='egg')
    >>> jobj['status'] == 'Server Error'
    True
    """


###############################################################################
# /api/lecture/[get|add]に関するテスト
###############################################################################
def test_about_lecture():
    u"""
    >>> name1 = 'Programming 1'
    >>> code1 = '0X123456789'
    >>> name2 = u'並行システム論'
    >>> code2 = '0X123456790'

    >>> clean_lectures()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_lecture_get_view,
    ...                                        access_lecture_add_view)

    >>> c = make_client()

    # authします
    >>> jobj1 = access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 最初は何も授業がありません
    >>> jobj2 = access_lecture_get_view(c)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['lectures'] == []
    True

    # 授業を追加します
    >>> jobj3a = access_lecture_add_view(c, name=name1, code=code1)
    >>> jobj3a['status'] == 'OK'
    True
    >>> jobj3a['created']
    True

    # あとで使うのでIDを保存
    >>> lecture1 = jobj3a['lecture']
    >>> id1 = lecture1['id']

    # lecture/getで確認してみます
    >>> jobj3b = access_lecture_get_view(c)
    >>> jobj3b['status'] == 'OK'
    True
    >>> lectures = jobj3b['lectures']
    >>> len(lectures) == 1
    True
    >>> lectures[0]['code'] == code1
    True
    >>> lectures[0]['name'] == name1
    True
    >>> lectures[0]['id'] == id1
    True

    # 二度目の追加は新規作成されません
    >>> jobj4a = access_lecture_add_view(c, name=name1, code=code1)
    >>> jobj4a['created']
    False

    # lecture/getで確認してみます
    >>> jobj4b = access_lecture_get_view(c)
    >>> lectures = jobj4b['lectures']
    >>> len(lectures)
    1

    # 他の授業も追加してみる
    >>> jobj5a = access_lecture_add_view(c, name=name2, code=code2)
    >>> jobj5a['created']
    True

    # lecture/getで確認してみます
    >>> jobj5b = access_lecture_get_view(c)
    >>> lectures = jobj5b['lectures']
    >>> len(lectures)
    2
    """


def test_about_lecture__forbidden():
    u"""
    >>> name = 'Programming 1'
    >>> code = '0X123456789'

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_lecture_get_view,
    ...                                        access_lecture_add_view)

    >>> c = make_client()

    # lecture/[get|add]はauthしていないとできません(Forbidden)
    >>> jobj1a = access_lecture_get_view(c)
    >>> jobj1a['status'] == 'Forbidden'
    True
    >>> jobj1b = access_lecture_add_view(c, name=name, code=code)
    >>> jobj1b['status'] == 'Forbidden'
    True

    # authします
    >>> jobj2 = access_auth_view(c)
    >>> jobj2['status'] == 'OK'
    True

    # authして初めてlecture/getできます
    >>> jobj3 = access_lecture_get_view(c)
    >>> jobj3['status'] == 'OK'
    True

    # authをして初めてlecture/addできます
    >>> jobj4a = access_lecture_add_view(c, name=name, code=code)
    >>> jobj4a['status'] == 'OK'
    True
    """


###############################################################################
# /api/lecture/timeline/ [GET|POST]に関するテスト
###############################################################################
def test_about_timeline():
    u"""
    何も投稿されていない授業のタイムラインを取得
    その授業のタイムラインに投稿
    その授業のタイムラインを取得

    >>> name = 'Arch1'
    >>> code = 't001'

    >>> clean_questions()
    >>> clean_lectures()
    >>> clean_users()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_timeline_get_view,
    ...                                        access_timeline_post_view,
    ...                                        access_lecture_add_view)


    # 下準備（授業の作成）
    >>> c0 = make_client()
    >>> jobj0a = access_auth_view(c0)
    >>> jobj0b = access_lecture_add_view(c0, name=name, code=code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = make_client()

    # authをします
    >>> jobj1 = access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 最初は何も投稿がありません
    >>> jobj2 = access_timeline_get_view(c, id=lecture_id)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['posts'] == []
    True

    # タイムラインに投稿します
    # before, afterをしていなければ最後尾に追加します
    # 今回はタイムラインが空なので指定してもしなくても一緒です
    >>> jobj3 = access_timeline_post_view(c, id=lecture_id, body=u'しりとり')
    >>> jobj3['status'] == 'OK'
    True

    >>> post = jobj3['post']

    # あとで使うのでID、実時間、仮想時間を保存
    >>> post1_id = post['id']
    >>> post1_time = post['time']
    >>> post1_vts = post['virtual_ts']

    ---------------------------------------------------------------------------
    # 先ほどの投稿の直前に投稿を挿入
    >>> jobj4 = access_timeline_post_view(c, id=lecture_id, body=u'おすし',
    ...                                   before_virtual_ts=0,
    ...                                   after_virtual_ts=post1_vts)
    >>> jobj4['status'] == 'OK'
    True
    >>> post = jobj4['post']
    >>> post2_id = post['id']
    >>> post2_time = post['time']
    >>> post2_vts = post['virtual_ts']

    # 実時間が減少することはない
    >>> post1_time < post2_time
    True

    # 仮想時間は小さくなってるはず
    >>> post2_vts < post1_vts
    True

    ---------------------------------------------------------------------------
    # post1, post2の間に投稿を挿入
    >>> jobj5 = access_timeline_post_view(c, id=lecture_id, body=u'しかえし',
    ...                                   before_virtual_ts=post2_vts,
    ...                                   after_virtual_ts=post1_vts)
    >>> jobj5['status'] == 'OK'
    True
    >>> post = jobj5['post']
    >>> post3_id = post['id']
    >>> post3_time = post['time']
    >>> post3_vts = post['virtual_ts']

    # 実時間が減少することはない
    >>> post1_time < post2_time < post3_time
    True

    # 仮想時間は中間値になってるはず
    >>> post2_vts < post3_vts < post1_vts
    True

    ---------------------------------------------------------------------------
    # before, afterをしていなければ最後尾に追加します
    >>> jobj6 = access_timeline_post_view(c, id=lecture_id, body=u'りんご')
    >>> jobj6['status'] == 'OK'
    True
    >>> post = jobj6['post']
    >>> post4_id = post['id']
    >>> post4_time = post['time']
    >>> post4_vts = post['virtual_ts']

    # 実時間が減少することはない
    >>> post1_time < post2_time < post3_time < post4_time
    True

    # 仮想時間は最大になってるはず
    >>> post2_vts < post3_vts < post1_vts < post4_vts
    True

    ---------------------------------------------------------------------------
    # タイムラインは仮想時間でソートされている
    >>> jobj7 = access_timeline_get_view(c, id=lecture_id)
    >>> jobj7['status'] == 'OK'
    True
    >>> posts = jobj7['posts']
    >>> len(posts) == 4
    True
    >>> (posts[0]['id'] == post2_id and
    ...  posts[1]['id'] == post3_id and
    ...  posts[2]['id'] == post1_id and
    ...  posts[3]['id'] == post4_id)
    True

    """


def test_about_timeline__badrequest():
    """
    This test will be added
    """
    u"""
    (注)Bad Request判定はForbidden, Not Found判定より先に行われるので
    認証や授業の作成は要らない

    before_virtual_ts, after_virtual_tsの片方だけ
    指定した場合はBad Requestを返す

    >>> clean_lectures()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_timeline_post_view)

    >>> c = make_client()

    # before_virtual_tsだけ指定します
    >>> jobj1 = access_timeline_post_view(c, id=1, body=u'beforeだけ',
    ...                                   before_virtual_ts=1000)
    >>> jobj1['status'] == 'Bad Request'
    True

    # after_virtual_tsだけ指定します
    >>> jobj2 = access_timeline_post_view(c, id=1, body=u'afterだけ',
    ...                                   after_virtual_ts=1000)
    >>> jobj2['status'] == 'Bad Request'
    True
    """


def test_about_timeline__forbidden():
    """
    This test will be added
    """
    u"""
    authしないでget/postした場合Forbiddenを返す

    >>> name = 'Arch1'
    >>> code = 't001'

    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_timeline_get_view,
    ...                                        access_timeline_post_view,
    ...                                        access_lecture_add_view)


    # 下準備（授業の作成）
    >>> c0 = make_client()
    >>> jobj0a = access_auth_view(c0)
    >>> jobj0b = access_lecture_add_view(c, name=name, code=code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = make_client()

    # lecture/timeline/(method [get|post])はauthしていないとできません（Forbidden）
    >>> jobj1a = access_timeline_get_view(c, id=lecture_id)
    >>> jobj1a['status'] == 'Forbidden'
    True
    >>> jobj1b = access_timeline_post_view(c, id=lecture_id, body=u'難しいな')
    >>> jobj1b['status'] == 'Forbidden'
    True

    # authをします
    >>> jobj2 = access_auth_view(c)
    >>> jobj2['status'] == 'OK'
    True

    # authして始めてgetできます
    >>> jobj3 = access_timeline_get_view(c, id=lecture_id)
    >>> jobj3['status'] == 'OK'
    True

    # authして始めてpostできます
    >>> jobj4 = access_timeline_post_view(c, id=lecture_id, body=u'なるほど')
    >>> jboj4['status'] == 'OK'
    True
    """


def test_about_timeline__notfound():
    """
    This test will be added
    """
    u"""
    存在しない授業IDでget/postした場合はNot Foundを返す

    >>> clean_questions()
    >>> clean_lectures()
    >>> from mysite.question.shortcuts import (make_client,
    ...                                        access_auth_view,
    ...                                        access_timeline_get_view,
    ...                                        access_timeline_post_view)


    >>> c = make_client()

    # authをします
    >>> jobj1 = access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 存在しないID(1)でgetします
    >>> jobj2 = access_timeline_get_view(c, id=1)
    >>> jobj2['status'] == 'Not Found'
    True

    # 存在しないID(1)でpostします
    >>> jobj3 = access_timeline_post_view(c, id=1, body=u'なるほど')
    >>> jobj3['status'] == 'Not Found'
    True
    """


def test_about_csrf():
    """
    >>> from django.test.client import Client
    >>> from mysite.question.shortcuts import (access_auth_view,
    ...                                        access_lecture_add_view,
    ...                                        access_timeline_post_view)


    # don't use csrf checking
    >>> c1 = Client()
    >>> jobj1 = access_auth_view(c1)

    >>> jobj2a = access_lecture_add_view(c1, name='hoge', code='foo')
    >>> jobj2a['status'] == 'OK'
    True
    >>> lecture_id1 = jobj2a['lecture']['id']

    # This test will be added
    # >>> jobj2b = access_timeline_post_view(c, id=lecture_id1, body='bar')
    # >>> jboj2b['status'] == 'OK'
    # True

    # use csrf checking
    >>> c2 = Client(enforce_csrf_checks=True)
    >>> jobj3 = access_auth_view(c2)
    >>> jobj4a = access_lecture_add_view(c2, name='hoge2', code='foo2')
    >>> jobj4a['status'] == 'OK'
    True
    >>> lecture_id2 = jobj4a['lecture']['id']

    # This test will be added
    # >>> jobj4b = access_timeline_post_view(c, id=lecture_id2, body='bar2')
    # >>> jboj4b['status'] == 'OK'
    # True
    """

    # もしviews.pyのlecture_add_view, lecture_timeline_viewに
    # @csrf_exemptデコレーターをつけないと、
    # c2を使う2つ目のテストは失敗する
    # （response.status_codeが200ではなく403となるので）
    # 一方c1を使う1つ目のテストは成功する。
    # なので、テストクライアントを作るときはenforce_csrf_checks=Trueを指定する


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
