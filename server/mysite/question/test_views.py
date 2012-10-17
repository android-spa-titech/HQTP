# -*- coding: utf-8 -*-


def get_vc_mock(user_key, user_secret):
    """
    Tweepy mock-up

    >>> from mysite.question.twutil.tw_util import get_vc

    If you want to use more twitter users,
    please add user into twitter_database.

    this method has 3 type behaviors
    1. If input key is in the database, and secret is correct
       then return user information

    >>> import mysite.question.twutil.consumer_info as ci
    >>> vc = get_vc(ci.spa_key, ci.spa_secret)
    >>> vc == dict(id=ci.spa_id, screen_name=ci.spa_screen_name,
    ...            name=ci.spa_name)
    True

    2. If input key is in the database, but secret is wrong
       then return empty dictionary (and server return not found)

    >>> vc = get_vc(ci.spa_key, 'egg')
    >>> vc == {}
    True

    3. If input key isn't in the database
       then return TypeError (and server return server error)

    >>> vc = get_vc('spam', 'egg')
    Traceback (most recent call last):
        ...
    TypeError: character mapping must return integer, None or unicode

    """
    import mysite.question.twutil.consumer_info as ci
    twitter_database = {
        ci.spa_key: dict(secret=ci.spa_secret,
                      vc=dict(id=ci.spa_id,
                              screen_name=ci.spa_screen_name,
                              name=ci.spa_name)
                      )
        }
    not_found_keys = set(['dummy key'])

    if user_key in twitter_database:
        user_info = twitter_database[user_key]
        if user_secret == user_info['secret']:
            return user_info['vc']
        else:
            return {}
    elif user_key in not_found_keys:
        return {}
    else:
        raise TypeError(('character mapping must return integer, '
                         'None or unicode'))


###############################################################################
# /api/auth/のAPIに関するテスト
###############################################################################
def test_about_api_auth():
    """
    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> 'created' in jobj
    True
    >>> user = jobj['user']
    >>> 'id' in user
    True
    >>> 'name' in user
    True
    >>> 'icon_url' in user
    True
    """

###############################################################################
# /api/auth/に関するテスト
###############################################################################


def test_about_auth():
    # test_about_api_authと統合できないか?
    u"""
    >>> c = shortcuts.make_client()

    # create user first time
    >>> jobj1 = shortcuts.access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True
    >>> jobj1['created']
    True
    >>> user = jobj1['user']
    >>> 'id' in user
    True
    >>> 'name' in user
    True

    # Test about icon
    >>> 'icon_url' in user
    True
    >>> user['icon_url'].find('api.twitter.com/1/users/profile_image') != -1
    True
    >>> user['icon_url'].find(user['name']) != -1
    True


    # user is crated only first time
    >>> c2 = shortcuts.make_client()
    >>> jobj2 = shortcuts.access_auth_view(c2)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['created']
    False
    """


def test_about_auth__badrequest():
    u"""
    access_token_key, access_token_secretが両方渡されないとBad Requestを返す

    >>> import json

    >>> c = shortcuts.make_client()

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


def test_about_auth__notfound():
    u"""
    TwitterのOAuthの正しくないkey, secretの場合Not Foundを返す

    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(
    ...     c, key='dummy key', secret='dummy secret')
    >>> jobj['status'] == 'Not Found'
    True
    """


def test_about_auth__servererror():
    u"""
    key, sercretによってはServer Errorを返す

    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c, key='spam', secret='egg')
    >>> jobj['status'] == 'Server Error'
    True
    """


###############################################################################
# /api/lecture/getのAPIに関するテスト
###############################################################################
def test_about_api_lecture_get():
    """
    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
    >>> jobj = shortcuts.access_lecture_get_view(c)
    >>> jobj['status'] == 'OK'
    True
    >>> 'lectures' in jobj
    True
    """


###############################################################################
# /api/lecture/addのAPIに関するテスト
###############################################################################
def test_about_api_lecture_add():
    """
    >>> name = 'Programming 1'
    >>> code = '0B123456789'
    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
    >>> jobj = shortcuts.access_lecture_add_view(c, name, code)
    >>> jobj['status'] == 'OK'
    True
    >>> 'created' in jobj
    True

    >>> lecture = jobj['lecture']
    >>> lecture['name'] == name
    True
    >>> lecture['code'] == code
    True
    >>> 'id' in lecture
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

    >>> c = shortcuts.make_client()

    # authします
    >>> jobj1 = shortcuts.access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 最初は何も授業がありません
    >>> jobj2 = shortcuts.access_lecture_get_view(c)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['lectures'] == []
    True

    # 授業を追加します
    >>> jobj3a = shortcuts.access_lecture_add_view(c, name1, code1)
    >>> jobj3a['status'] == 'OK'
    True
    >>> jobj3a['created']
    True

    # あとで使うのでIDを保存
    >>> lecture1 = jobj3a['lecture']
    >>> id1 = lecture1['id']

    # lecture/getで確認してみます
    >>> jobj3b = shortcuts.access_lecture_get_view(c)
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
    >>> jobj4a = shortcuts.access_lecture_add_view(c, name1, code1)
    >>> jobj4a['created']
    False

    # lecture/getで確認してみます
    >>> jobj4b = shortcuts.access_lecture_get_view(c)
    >>> lectures = jobj4b['lectures']
    >>> len(lectures)
    1

    # 他の授業も追加してみる
    >>> jobj5a = shortcuts.access_lecture_add_view(c, name2, code2)
    >>> jobj5a['created']
    True

    # lecture/getで確認してみます
    >>> jobj5b = shortcuts.access_lecture_get_view(c)
    >>> lectures = jobj5b['lectures']
    >>> len(lectures)
    2
    """


def test_about_lecture__forbidden():
    u"""
    authしないでget/addした場合Forbiddenを返す

    >>> name = 'Programming 1'
    >>> code = '0X123456789'

    >>> c = shortcuts.make_client()

    # lecture/[get|add]はauthしていないとできません(Forbidden)
    >>> jobj1a = shortcuts.access_lecture_get_view(c)
    >>> jobj1a['status'] == 'Forbidden'
    True
    >>> jobj1b = shortcuts.access_lecture_add_view(c, name, code)
    >>> jobj1b['status'] == 'Forbidden'
    True

    # authします
    >>> jobj2 = shortcuts.access_auth_view(c)
    >>> jobj2['status'] == 'OK'
    True

    # authして初めてlecture/getできます
    >>> jobj3 = shortcuts.access_lecture_get_view(c)
    >>> jobj3['status'] == 'OK'
    True

    # authをして初めてlecture/addできます
    >>> jobj4a = shortcuts.access_lecture_add_view(c, name, code)
    >>> jobj4a['status'] == 'OK'
    True
    """


###############################################################################
# /api/lecture/timeline/ GET APIに関するテスト
###############################################################################
def test_about_api_timeline_get():
    u"""
    >>> name = 'Arch1'
    >>> code = 't001'

    # 下準備（授業の作成）
    >>> c0 = shortcuts.make_client()
    >>> jobj0a = shortcuts.access_auth_view(c0)
    >>> jobj0b = shortcuts.access_lecture_add_view(c0, name, code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
    >>> jobj = shortcuts.access_timeline_get_view(c, lecture_id)
    >>> jobj['status'] == 'OK'
    True
    >>> 'posts' in jobj
    True
    """


###############################################################################
# /api/lecture/timeline/ POST APIに関するテスト
###############################################################################
def test_about_api_timeline_post():
    u"""
    >>> name = 'Arch1'
    >>> code = 't001'
    >>> body = u'MIPSとは'

    # 下準備（授業の作成）
    >>> c0 = shortcuts.make_client()
    >>> jobj0a = shortcuts.access_auth_view(c0)
    >>> jobj0b = shortcuts.access_lecture_add_view(c0, name, code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
    >>> jobj = shortcuts.access_timeline_post_view(c, lecture_id, body,
    ...                                            before_virtual_ts=1000,
    ...                                            after_virtual_ts=2000)
    >>> jobj['status'] == 'OK'
    True
    >>> post = jobj['post']
    >>> 'id' in post
    True

    # 追加先授業の情報は正しいか
    >>> lecture = post['lecture']
    >>> lecture['id'] == lecture_id
    True
    >>> lecture['name'] == name
    True
    >>> lecture['code'] == code
    True

    # 投稿内容の情報は正しいか
    >>> post['body'] == body
    True

    # 投稿ユーザーの情報は正しいか
    >>> user = post['user']
    >>> 'id' in user and 'name' in user and 'icon_url' in user
    True

    >>> 'time' in post
    True
    >>> 'virtual_ts' in post
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

    >>> from time import sleep


    # 下準備（授業の作成）
    >>> c0 = shortcuts.make_client()
    >>> jobj0a = shortcuts.access_auth_view(c0)
    >>> jobj0b = shortcuts.access_lecture_add_view(c0, name, code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = shortcuts.make_client()

    # authをします
    >>> jobj1 = shortcuts.access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 最初は何も投稿がありません
    >>> jobj2 = shortcuts.access_timeline_get_view(c, lecture_id)
    >>> jobj2['status'] == 'OK'
    True
    >>> jobj2['posts'] == []
    True

    # タイムラインに投稿します
    # before, afterをしていなければ最後尾に追加します
    # 今回はタイムラインが空なので指定してもしなくても一緒です
    >>> jobj3 = shortcuts.access_timeline_post_view(c, lecture_id,
    ...                                             body=u'しりとり')
    >>> jobj3['status'] == 'OK'
    True

    >>> post = jobj3['post']

    # あとで使うのでID、実時間、仮想時間を保存
    >>> post1_id = post['id']
    >>> post1_time = post['time']
    >>> post1_vts = post['virtual_ts']

    ---------------------------------------------------------------------------
    # 先ほどの投稿の直前に投稿を挿入
    >>> sleep(1)
    >>> jobj4 = shortcuts.access_timeline_post_view(c, lecture_id,
    ...                                             body=u'おすし',
    ...                                             before_virtual_ts=0,
    ...                                             after_virtual_ts=post1_vts)
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
    >>> sleep(1)
    >>> jobj5 = shortcuts.access_timeline_post_view(
    ...     c, lecture_id, body=u'しかえし', before_virtual_ts=post2_vts,
    ...     after_virtual_ts=post1_vts)
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
    >>> sleep(1)
    >>> jobj6 = shortcuts.access_timeline_post_view(c, lecture_id,
    ...                                             body=u'りんご')
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
    >>> jobj7 = shortcuts.access_timeline_get_view(c, lecture_id)
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
    u"""
    (注)Bad Request判定はForbidden, Not Found判定より先に行われるので
    認証や授業の作成は要らない

    before_virtual_ts, after_virtual_tsの片方だけ
    指定した場合はBad Requestを返す

    >>> c = shortcuts.make_client()

    # before_virtual_tsだけ指定します
    >>> jobj1 = shortcuts.access_timeline_post_view(
    ...     c, lecture_id=1, body=u'beforeだけ', before_virtual_ts=1000)
    >>> jobj1['status'] == 'Bad Request'
    True

    # after_virtual_tsだけ指定します
    >>> jobj2 = shortcuts.access_timeline_post_view(
    ...     c, lecture_id=1, body=u'afterだけ', after_virtual_ts=1000)
    >>> jobj2['status'] == 'Bad Request'
    True
    """


def test_about_timeline__forbidden():
    u"""
    authしないでget/postした場合Forbiddenを返す

    >>> name = 'Arch1'
    >>> code = 't001'

    # 下準備（授業の作成）
    >>> c0 = shortcuts.make_client()
    >>> jobj0a = shortcuts.access_auth_view(c0)
    >>> jobj0b = shortcuts.access_lecture_add_view(c0, name=name, code=code)
    >>> lecture_id = jobj0b['lecture']['id']

    >>> c = shortcuts.make_client()

    # lecture/timeline/(method [get|post])はauthしていないとできません
    # (Forbidden)
    >>> jobj1a = shortcuts.access_timeline_get_view(c, lecture_id)
    >>> jobj1a['status'] == 'Forbidden'
    True
    >>> jobj1b = shortcuts.access_timeline_post_view(c, lecture_id,
    ...                                              body=u'難しいな')
    >>> jobj1b['status'] == 'Forbidden'
    True

    # authをします
    >>> jobj2 = shortcuts.access_auth_view(c)
    >>> jobj2['status'] == 'OK'
    True

    # authして始めてgetできます
    >>> jobj3 = shortcuts.access_timeline_get_view(c, lecture_id)
    >>> jobj3['status'] == 'OK'
    True

    # authして始めてpostできます
    >>> jobj4 = shortcuts.access_timeline_post_view(c, lecture_id,
    ...                                             body=u'なるほど')
    >>> jobj4['status'] == 'OK'
    True
    """


def test_about_timeline__notfound():
    u"""
    存在しない授業IDでget/postした場合はNot Foundを返す

    >>> c = shortcuts.make_client()

    # authをします
    >>> jobj1 = shortcuts.access_auth_view(c)
    >>> jobj1['status'] == 'OK'
    True

    # 存在しないID(1)でgetします
    >>> jobj2 = shortcuts.access_timeline_get_view(c, lecture_id=1)
    >>> jobj2['status'] == 'Not Found'
    True

    # 存在しないID(1)でpostします
    >>> jobj3 = shortcuts.access_timeline_post_view(c, lecture_id=1,
    ...                                             body=u'なるほど')
    >>> jobj3['status'] == 'Not Found'
    True
    """


def test_about_csrf():
    """
    もしviews.pyのlecture_add_view, lecture_timeline_viewに
    @csrf_exemptデコレーターをつけないと、
    c2を使う2つ目のテストは失敗する
    （response.status_codeが200ではなく403となるので）
    一方c1を使う1つ目のテストは成功する。
    なので、テストクライアントを作るときはenforce_csrf_checks=Trueを指定する

    >>> from django.test.client import Client

    # don't use csrf checking
    >>> c1 = Client()
    >>> jobj1 = shortcuts.access_auth_view(c1)

    >>> jobj2a = shortcuts.access_lecture_add_view(c1, name='hoge', code='foo')
    >>> jobj2a['status'] == 'OK'
    True
    >>> lecture_id1 = jobj2a['lecture']['id']

    >>> jobj2b = shortcuts.access_timeline_post_view(c1, lecture_id1,
    ...                                              body='bar')
    >>> jobj2b['status'] == 'OK'
    True

    # use csrf checking
    >>> c2 = Client(enforce_csrf_checks=True)
    >>> jobj3 = shortcuts.access_auth_view(c2)
    >>> jobj4a = shortcuts.access_lecture_add_view(
    ...     c2, name='hoge2', code='foo2')
    >>> jobj4a['status'] == 'OK'
    True
    >>> lecture_id2 = jobj4a['lecture']['id']

    >>> jobj4b = shortcuts.access_timeline_post_view(
    ...     c2, lecture_id2, body='bar2')
    >>> jobj4b['status'] == 'OK'
    True
    """


def test_about_user_profile():
    """
    UserProfileが正しく機能しているか確認

    >>> from django.contrib.auth.models import User


    >>> c = shortcuts.make_client()
    >>> jobj = shortcuts.access_auth_view(c)
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

from mysite.question.models import Lecture, Post
from django.contrib.auth.models import User
import mysite.question.twutil.tw_util as tw_util
import mysite.question.shortcuts as shortcuts


def setup(test):
    Post.objects.all().delete()
    Lecture.objects.all().delete()
    User.objects.all().delete()

    test.globs['shortcuts'] = shortcuts

    test.globs['real_get_vc'] = tw_util.get_vc
    tw_util.get_vc = get_vc_mock


def teardown(test):
    tw_util.get_vc = test.globs['real_get_vc']


def load_tests(loader, tests, ignore):
    import doctest
    tests.addTests(doctest.DocTestSuite(setUp=setup, tearDown=teardown))
    return tests
