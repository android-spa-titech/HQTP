# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
from mysite.question.models import Lecture
from django.test import TestCase
from django.test.client import Client


class AuthenticateTests(TestCase):
    def test_access_auth_view(self):
        j_auth = sc.access_auth_view(self.client)
        self.assertEqual(j_auth['status'], 'OK')

    def test_access_auth_view_second(self):
        # second log-in user
        sc.access_auth_view(self.client)
        c2 = Client()
        j_auth2 = sc.access_auth_view(c2)
        self.assertFalse(j_auth2['created'])


class AuthenticateFailTests(TestCase):
    def test_auth_only_key(self):
        j_bad = sc.access_auth_view(self.client, access_token_key='ONLY_KEY')
        self.assertEqual(j_bad['status'], 'Bad Request')

    def test_auth_only_secret(self):
        j_bad = sc.access_auth_view(self.client,
                                    access_token_secret='ONLY_SECRET')
        self.assertEqual(j_bad['status'], 'Bad Request')

    def test_invalid_key_and_secret(self):
        # 値によっては Server Error
        j_not_found = sc.access_auth_view(self.client, access_token_key='spam',
                                          access_token_secret='egg')
        self.assertEqual(j_not_found['status'], 'Server Error')


class LectureTests(TestCase):
    # 前提条件: すでに1つだけ授業が登録してあり、ユーザーが認証済み
    fixtures = ['test_user.json', 'test_lecture.json']

    def setUp(self):
        self.client.login(username='testuser', password='testpassword')

    def test_get_lecture(self):
        # GETに成功すれば、長さ1の授業リストが返ってきます
        j_lec_get1 = sc.access_lecture_get_view(self.client)
        self.assertEqual(len(j_lec_get1['lectures']), 1)

    def test_add_existing_lecture(self):
        # 一度追加した授業は新規作成されません
        j_lec_add2 = sc.access_lecture_add_view(
            self.client, name=u'Software Test', code=u'76036')
        self.assertFalse(j_lec_add2['created'])

    def test_add_new_lecture(self):
        # 他の授業も追加してみます
        j_lec_get2 = sc.access_lecture_add_view(
            self.client, name=u'オペレーティングシステム特論', code=u'76001')
        self.assertEqual(j_lec_get2['status'], 'OK')


class LectureFailTests(TestCase):
    def test_lecture_add_only_name(self):
        # パラメタが間違っているとBad Request
        # NOTE: Bad Request の判定は Forbidden より先に行われる
        j_add_only_name = sc.access_template(self.client.post, 'lecture/add',
                                             name='Only Name')
        self.assertEqual(j_add_only_name['status'], 'Bad Request')

    def test_lecture_add_only_code(self):
        j_add_only_code = sc.access_template(self.client.post, 'lecture/add',
                                             code='Only Code')
        self.assertEqual(j_add_only_code['status'], 'Bad Request')

    def test_lecture_get_without_auth(self):
        # 認証しないでLecture get/add しようとするとForbidden
        j_get_fbd = sc.access_lecture_get_view(self.client)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_lecture_add_without_auth(self):
        j_add_fbd = sc.access_lecture_add_view(self.client,
            name='Not Authenticated', code='65536')
        self.assertEqual(j_add_fbd['status'], 'Forbidden')


class TimeLineTests(TestCase):
    # 前提条件: 1つの授業と2つの投稿が存在する
    fixtures = ['test_user.json', 'test_lecture.json', 'test_posts.json']

    def setUp(self):
        self.client.login(username='testuser', password='testpassword')

    def assertListSorted(self, *args):
        self.assertListEqual(sorted(args), list(args))

    def get_timeline_from_db(self, lecture_id):
        # 授業IDを指定し、データベースから直接タイムラインを取得
        lec = Lecture.objects.get(pk=lecture_id)
        return [p.to_dict() for p in lec.post_set.order_by('virtual_ts')]

    def test_timeline_get_view(self):
        # GETに成功すれば、長さ2のタイムラインが返ってくる
        j_get = sc.access_timeline_get_view(self.client, lecture_id=1)
        self.assertEqual(len(j_get['posts']), 2)

    def test_post_to_latest(self):
        # 最新のタイムラインに投稿
        sc.access_timeline_post_view(
            self.client, lecture_id=1, body=u'3rd post')
        timeline = self.get_timeline_from_db(lecture_id=1)
        # NOTE: タイムラインは仮想タイムスタンプの順にソートされる
        self.assertListSorted(timeline[0]['id'],
                              timeline[1]['id'],
                              timeline[2]['id'])

    def test_post_to_between_posts(self):
        # 2つのPOSTの中間の仮想タイムスタンプを指定
        sc.access_timeline_post_view(
            self.client, lecture_id=1, body=u'1.5th post',
            before_virtual_ts=135044899528200600,
            after_virtual_ts=135044902317511100)
        timeline = self.get_timeline_from_db(lecture_id=1)
        # 仮想タイムスタンプと投稿ID(通し番号)の順番が異なる
        self.assertListSorted(timeline[0]['id'],
                              timeline[2]['id'],
                              timeline[1]['id'])

    def test_timeline_get__not_found(self):
        # 存在しない授業にget/postしようとすると Not Found
        j_get_not = sc.access_timeline_get_view(self.client, lecture_id=42)
        self.assertEqual(j_get_not['status'], 'Not Found')

    def test_timeline_post__not_found(self):
        j_post_not = sc.access_timeline_post_view(self.client, lecture_id=42,
            body=u'チャドの授業が・・・消えた・・・?')
        self.assertEqual(j_post_not['status'], 'Not Found')


class TimeLineFailTests(TestCase):
    fixtures = ['test_lecture.json']

    def test_post_without_lecture_id(self):
        j_bad = sc.access_template(self.client.post, 'lecture/timeline',
                                   body=u'授業を指定しないとこうなる')
        self.assertEqual(j_bad['status'], 'Bad Request')

    def test_post_without_body(self):
        j_bad = sc.access_template(self.client.post, 'lecture/timeline', id=1)
        self.assertEqual(j_bad['status'], 'Bad Request')

    def test_post_only_before_vts(self):
        # before_virtual_ts, after_virtual_ts 片方だけだと Bad Request
        j_only_before = sc.access_timeline_post_view(self.client, lecture_id=1,
            body=u'投稿できないよ!', before_virtual_ts=65536)
        self.assertEqual(j_only_before['status'], 'Bad Request')

    def test_post_only_after_vts(self):
        j_only_after = sc.access_timeline_post_view(self.client, lecture_id=1,
            body=u'投稿できないよ!', after_virtual_ts=65536)
        self.assertEqual(j_only_after['status'], 'Bad Request')

    def test_timeline_get_without_auth(self):
        # 認証しないで timeline get/post したら Forbidden
        j_get_fbd = sc.access_timeline_get_view(self.client, lecture_id=1)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_timeline_post_without_auth(self):
        j_post_fbd = sc.access_timeline_post_view(self.client, lecture_id=1,
            body=u'認証が切れたら、俺は投稿もできないのかよ')
        self.assertEqual(j_post_fbd['status'], 'Forbidden')


class UserGetTests(TestCase):
    fixtures = ['test_user.json', 'test_user_profile.json']
    # 認証は必要ありません

    def test_get_user_info(self):
        j_user = sc.access_user_get_view(self.client, 1)
        self.assertEqual(j_user['user']['name'], 'android_spa')

    def test_get_user__bad_request(self):
        # IDを指定しないとBad Request
        j_bad = sc.access_template(self.client.get, 'user')
        self.assertEqual(j_bad['status'], 'Bad Request')

    def test_get_user__not_found(self):
        j_not = sc.access_user_get_view(self.client, 42)
        self.assertEqual(j_not['status'], 'Not Found')
