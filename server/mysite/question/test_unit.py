# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
from mysite.question.models import Lecture
from django.test import TestCase
from django.test.client import Client
from json import loads
import re
import os
import mysite.question.test_views as test_views
import mysite.question.twutil.tw_util as tw_util
import mysite.question.image_utils as image_utils
import mysite.question.twutil.consumer_info as ci


class AuthenticateTests(TestCase):
    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)
        self._original_get_vc = tw_util.get_vc
        tw_util.get_vc = test_views.get_vc_mock
        self._original_get_img = image_utils.get_img
        image_utils.get_img = test_views.get_img_mock

        # 既にtwiconが保存されていれば削除
        relative_pathname = os.path.join('twicon', str(ci.spa_id))
        self.absolute_pathname = (
            image_utils.build_media_absolute_pathname(relative_pathname))
        if os.path.exists(self.absolute_pathname):
            os.remove(self.absolute_pathname)

    def tearDown(self):
        tw_util.get_vc = self._original_get_vc
        image_utils.get_img = self._original_get_img

        # テストで保存されたtwiconを削除
        if os.path.exists(self.absolute_pathname):
            os.remove(self.absolute_pathname)

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
        self._original_get_vc = tw_util.get_vc
        tw_util.get_vc = test_views.get_vc_mock
        self._original_get_img = image_utils.get_img
        image_utils.get_img = test_views.get_img_mock

    def tearDown(self):
        tw_util.get_vc = self._original_get_vc
        image_utils.get_img = self._original_get_img

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


class LectureTests(TestCase):
    # 前提条件: すでに1つだけ授業が登録してあり、ユーザーが認証済み
    fixtures = ['test_user.json', 'test_lecture.json']

    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)
        self.c.login(username='testuser', password='testpassword')

    def test_get_lecture(self):
        # GETに成功すれば、長さ1の授業リストが返ってきます
        j_lec_get1 = sc.access_lecture_get_view(self.c)
        self.assertEqual(len(j_lec_get1['lectures']), 1)

    def test_add_existing_lecture(self):
        # 一度追加した授業は新規作成されません
        j_lec_add2 = sc.access_lecture_add_view(
            self.c, name=u'Software Test', code=u'76036')
        self.assertFalse(j_lec_add2['created'])

    def test_add_new_lecture(self):
        # 他の授業も追加してみます
        j_lec_get2 = sc.access_lecture_add_view(
            self.c, name=u'オペレーティングシステム特論', code=u'76001')
        self.assertEqual(j_lec_get2['status'], 'OK')


class LectureFailTests(TestCase):
    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)

    def test_lecture_add_only_name(self):
        # パラメタが間違っているとBad Request
        # NOTE: Bad Request の判定は Forbidden より先に行われる
        res_name = self.c.post('/api/lecture/add/', dict(name='Only Name'))
        j_add_only_name = loads(res_name.content)
        self.assertEqual(j_add_only_name['status'], 'Bad Request')

    def test_lecture_add_only_code(self):
        res_code = self.c.post('/api/lecture/add/', dict(code='Only Code'))
        j_add_only_code = loads(res_code.content)
        self.assertEqual(j_add_only_code['status'], 'Bad Request')

    def test_lecture_get_without_auth(self):
        # 認証しないでLecture get/add しようとするとForbidden
        j_get_fbd = sc.access_lecture_get_view(self.c)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_lecture_add_without_auth(self):
        j_add_fbd = sc.access_lecture_add_view(self.c,
            name='Not Authenticated', code='65536')
        self.assertEqual(j_add_fbd['status'], 'Forbidden')


class TimeLineTests(TestCase):
    # 前提条件: 1つの授業と2つの投稿が存在する
    fixtures = ['test_user.json', 'test_lecture.json', 'test_posts.json']

    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)
        self.c.login(username='testuser', password='testpassword')

    def assertListSorted(self, *args):
        self.assertListEqual(sorted(args), list(args))

    def get_timeline_from_db(self, lecture_id):
        # 授業IDを指定し、データベースから直接タイムラインを取得
        lec = Lecture.objects.get(pk=lecture_id)
        return [p.to_dict() for p in lec.post_set.order_by('virtual_ts')]

    def test_timeline_get_view(self):
        # GETに成功すれば、長さ2のタイムラインが返ってくる
        j_get = sc.access_timeline_get_view(self.c, lecture_id=1)
        self.assertEqual(len(j_get['posts']), 2)

    def test_post_to_latest(self):
        # 最新のタイムラインに投稿
        sc.access_timeline_post_view(self.c, lecture_id=1, body=u'3rd post')
        timeline = self.get_timeline_from_db(lecture_id=1)
        # NOTE: タイムラインは仮想タイムスタンプの順にソートされる
        self.assertListSorted(timeline[0]['id'],
                              timeline[1]['id'],
                              timeline[2]['id'])

    def test_post_to_between_posts(self):
        # 2つのPOSTの中間の仮想タイムスタンプを指定
        sc.access_timeline_post_view(self.c, lecture_id=1, body=u'1.5th post',
            before_virtual_ts=135044899528200600,
            after_virtual_ts=135044902317511100)
        timeline = self.get_timeline_from_db(lecture_id=1)
        # 仮想タイムスタンプと投稿ID(通し番号)の順番が異なる
        self.assertListSorted(timeline[0]['id'],
                              timeline[2]['id'],
                              timeline[1]['id'])

    def test_timeline_get__not_found(self):
        # 存在しない授業にget/postしようとすると Not Found
        j_get_not = sc.access_timeline_get_view(self.c, lecture_id=42)
        self.assertEqual(j_get_not['status'], 'Not Found')

    def test_timeline_post__not_found(self):
        j_post_not = sc.access_timeline_post_view(self.c, lecture_id=42,
            body=u'チャドの授業が・・・消えた・・・?')
        self.assertEqual(j_post_not['status'], 'Not Found')


class TimeLineFailTests(TestCase):
    fixtures = ['test_lecture.json']

    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)

    def test_post_only_before_vts(self):
        # before_virtual_ts, after_virtual_ts 片方だけだと Bad Request
        j_only_before = sc.access_timeline_post_view(self.c, lecture_id=1,
            body=u'投稿できないよ!', before_virtual_ts=65536)
        self.assertEqual(j_only_before['status'], 'Bad Request')

    def test_post_only_after_vts(self):
        j_only_after = sc.access_timeline_post_view(self.c, lecture_id=1,
            body=u'投稿できないよ!', after_virtual_ts=65536)
        self.assertEqual(j_only_after['status'], 'Bad Request')

    def test_timeline_get_without_auth(self):
        # 認証しないで timeline get/post したら Forbidden
        j_get_fbd = sc.access_timeline_get_view(self.c, lecture_id=1)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_timeline_post_without_auth(self):
        j_post_fbd = sc.access_timeline_post_view(self.c, lecture_id=1,
            body=u'認証が切れたら、俺は投稿もできないのかよ')
        self.assertEqual(j_post_fbd['status'], 'Forbidden')


class ImagePostTests(TestCase):
    """
    Timelineへの画像の投稿に関して
    """
    # 前提条件: すでに1つだけ授業が登録してあり、ユーザーが認証済み
    fixtures = ['test_user.json', 'test_lecture.json']

    def setUp(self):
        self.c = Client(enforce_csrf_checks=True)
        self.c.login(username='testuser', password='testpassword')

        # Uploadするファイルの絶対パス
        relative_pathname_src = 'default_twicon'
        self.absolute_pathname_src = (
            image_utils.build_media_absolute_pathname(relative_pathname_src))

        # 存在確認
        errmsg = 'File [%s] is not Found' % relative_pathname_src
        assert os.path.exists(self.absolute_pathname_src), errmsg

        # 保存後のファイルの絶対パス
        relative_pathname = os.path.join('uploads', 'img_1')
        self.absolute_pathname = (
            image_utils.build_media_absolute_pathname(relative_pathname))

        # テストなどで保存された画像ファイルがあれば削除
        if os.path.exists(self.absolute_pathname):
            os.remove(self.absolute_pathname)

    def tearDown(self):
        # テストで保存された画像ファイルを削除
        if os.path.exists(self.absolute_pathname):
            os.remove(self.absolute_pathname)

    def test_post_image(self):
        image = open(self.absolute_pathname_src)
        j_post_image = sc.access_timeline_post_view(
            self.c, lecture_id=1, image=image)

        # image_urlが適切に設定されていることを確認
        image_url = j_post_image['post']['image_url']
        ptn = re.compile('http://.+/site_media/uploads/img_(\d)')
        img_id = int(ptn.match(image_url).group(1))
        self.assertEqual(img_id, j_post_image['post']['id'])

        # 画像がちゃんと保存されていることを確認
        self.assertTrue(os.path.exists(self.absolute_pathname))
