# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
from mysite.question.models import Lecture
from django.test import TestCase
from django.test.client import Client
from json import loads


def get_lecture_id():
    # 呼び出された時点でのDBから0番目のLectureモデルのディクショナリを取得する
    # Fixtureでテスト用のLectureモデルを投入した時の使用を想定
    return Lecture.objects.all()[0].to_dict()['id']


def get_lecture_timeline(lec_id):
    lec = Lecture.objects.get(pk=lec_id)
    return [p.to_dict() for p in lec.post_set.order_by('virtual_ts')]


class AuthenticatedTestCase(TestCase):
    # 認証済みのクライアントを使用できる
    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)
        self.c.login(username='testuser', password='testpassword')


class AuthenticateTests(TestCase):
    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)

    def test_access_auth_view(self):
        j_auth = sc.access_auth_view(self.c)
        self.assertEqual(j_auth['status'], 'OK')

    def test_access_auth_view_second(self):
        # second log-in user
        sc.access_auth_view(self.c)
        c2 = Client(enforce_csrf_checks=True)
        j_auth2 = sc.access_auth_view(c2)
        self.assertFalse(j_auth2['created'])


class AuthenticateFailTests(TestCase):
    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)

    def test_auth_only_key(self):
        url_key = '/api/auth/?access_token_key=BAD_REQUEST_KEY'
        response = self.c.get(url_key)
        j_only_key = loads(response.content)
        self.assertEqual(j_only_key['status'], 'Bad Request')

    def test_auth_only_secret(self):
        url_secret = '/api/auth/?access_token_secret=BAD_REQUEST_SECRET'
        response = self.c.get(url_secret)
        j_only_secret = loads(response.content)
        self.assertEqual(j_only_secret['status'], 'Bad Request')

    def test_invalid_key_and_secret(self):
        # 値によっては Server Error
        j_not_found = sc.access_auth_view(self.c, 'spam', 'egg')
        self.assertEqual(j_not_found['status'], 'Server Error')


class LectureTests(AuthenticatedTestCase):
    # 前提条件: すでに1つだけ授業が登録してあり、ユーザーが認証済み
    fixtures = ['test_user.json', 'test_lecture.json']

    def test_add_lecture(self):
        # GETに成功すれば、リストに1つだけ授業が存在する状態になります
        j_lec_get1 = sc.access_lecture_get_view(self.c)
        self.assertEqual(len(j_lec_get1['lectures']), 1)

    def test_add_existing_lecture(self):
        # 一度追加した授業は新規作成されません
        j_lec_add2 = sc.access_lecture_add_view(
            self.c, name=u'Software Test', code=u'76036')
        self.assertFalse(j_lec_add2['created'])

    def test_add_another_lecture(self):
        # 他の授業も追加してみます
        j_lec_get2 = sc.access_lecture_add_view(
            self.c, name=u'オペレーティングシステム特論', code=u'76001')
        self.assertEqual(j_lec_get2['status'], 'OK')


class LectureFailTests(TestCase):
    def setUp(self):
        self.c = Client()
        self.name = u'コンピュータリテラシ'
        self.code = u'761'

    def test_lecture_add_only_name(self):
        # パラメタが間違っているとBad Request
        # NOTE: Bad Request の判定は Forbidden より先に行われる
        res_name = self.c.post('/api/lecture/add/', dict(name=self.name))
        j_add_only_name = loads(res_name.content)
        self.assertEqual(j_add_only_name['status'], 'Bad Request')

    def test_lecture_add_only_code(self):
        res_code = self.c.post('/api/lecture/add/', dict(code=self.code))
        j_add_only_code = loads(res_code.content)
        self.assertEqual(j_add_only_code['status'], 'Bad Request')

    def test_lecture_get_without_auth(self):
        # 認証しないでLecture get/add しようとするとForbidden
        j_get_fbd = sc.access_lecture_get_view(self.c)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_lecture_add_without_auth(self):
        j_add_fbd = sc.access_lecture_add_view(self.c, self.name, self.code)
        self.assertEqual(j_add_fbd['status'], 'Forbidden')


class TimeLineTests(AuthenticatedTestCase):
    # 前提条件: 1つの授業と2つの投稿が存在する
    fixtures = ['test_user.json', 'test_lecture.json', 'test_posts.json']

    def assertListSorted(self, *args):
        self.assertListEqual(sorted(args), list(args))

    def test_timeline_get_view(self):
        j_get = sc.access_timeline_get_view(self.c, get_lecture_id())
        self.assertEqual(len(j_get['posts']), 2)

    def test_post_to_latest(self):
        # 最新のタイムラインに投稿
        sc.access_timeline_post_view(self.c, get_lecture_id(),
                                     body='3rd post')
        timeline = get_lecture_timeline(get_lecture_id())
        # NOTE: タイムラインは仮想タイムスタンプの順にソートされる
        self.assertListSorted(timeline[0]['id'],
                              timeline[1]['id'],
                              timeline[2]['id'])

    def test_post_to_between_posts(self):
        bvts = get_lecture_timeline(get_lecture_id())[0]['virtual_ts']
        avts = get_lecture_timeline(get_lecture_id())[1]['virtual_ts']
        sc.access_timeline_post_view(self.c, get_lecture_id(),
            body='1.5th post', before_virtual_ts=bvts, after_virtual_ts=avts)
        timeline = get_lecture_timeline(get_lecture_id())
        self.assertListSorted(timeline[0]['id'],
                              timeline[2]['id'],
                              timeline[1]['id'])

    def test_timeline_get__not_found(self):
        # 存在しない授業にget/postしようとすると Not Found
        j_get_not = sc.access_timeline_get_view(self.c, 42)
        self.assertEqual(j_get_not['status'], 'Not Found')

    def test_timeline_post__not_found(self):
        j_post_not = sc.access_timeline_post_view(self.c, 42,
            body=u'チャドの授業が・・・消えた・・・?')
        self.assertEqual(j_post_not['status'], 'Not Found')


class TimeLineFailTests(TestCase):
    fixtures = ['test_lecture.json']

    def setUp(self):
        self.c = Client()
        self.lec_id = get_lecture_id()

    def test_post_only_before_vts(self):
        # before_virtual_ts, after_virtual_ts 片方だけだと Bad Request
        j_only_before = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'投稿できないよ!', before_virtual_ts=65536)
        self.assertEqual(j_only_before['status'], 'Bad Request')

    def test_post_only_after_vts(self):
        j_only_after = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'投稿できないよ!', after_virtual_ts=65536)
        self.assertEqual(j_only_after['status'], 'Bad Request')

    def test_timeline_get_without_auth(self):
        # 認証しないで timeline get/post したら Forbidden
        j_get_fbd = sc.access_timeline_get_view(self.c, self.lec_id)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_timeline_post_without_auth(self):
        j_post_fbd = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'認証が切れたら、俺は投稿もできないのかよ')
        self.assertEqual(j_post_fbd['status'], 'Forbidden')
