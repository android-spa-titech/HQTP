# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
from django.test import TestCase
from django.test.client import Client
from django.utils import unittest
from json import loads


class PostMetaDataFromJSON:
    def __init__(self, jobj):
        """
        以下の操作をいちいち書くのが面倒だったのでクラスにした
        post = jobj['post']
        post1_id = post['id']
        post1_time = post['time']
        post1_vts = post['virtual_ts']
        """
        self.pid = jobj['post']['id']
        self.time = jobj['post']['time']
        self.vts = jobj['post']['virtual_ts']


class AuthenticatedTestCase(TestCase):
    # 認証済みのクライアントを使用できる
    # TODO: access_auth_view()を用いない認証方法に変える
    def make_auth_client(self):
        self.c = Client()
        self.j_auth = sc.access_auth_view(self.c)


class AuthenticateTests(TestCase):
    # このクラスだけはaccess_auth_view()を用いる
    def setUp(self):
        self.c = Client()
        self.j_auth = sc.access_auth_view(self.c)

    def test_about_user_info(self):
        self.assertEqual(self.j_auth['status'], 'OK')

    def test_about_second_auth(self):
        # second log-in user
        c2 = sc.make_client()
        j_auth2 = sc.access_auth_view(c2)
        self.assertFalse(j_auth2['created'])


class AuthenticateFailTests(TestCase):
    def setUp(self):
        self.c = Client()

    def test_about_auth_only_key(self):
        url_key = '/api/auth/?access_token_key=BAD_REQUEST_KEY'
        response = self.c.get(url_key)
        j_only_key = loads(response.content)
        self.assertEqual(j_only_key['status'], 'Bad Request')

    def test_about_auth_only_secret(self):
        url_secret = '/api/auth/?access_token_secret=BAD_REQUEST_SECRET'
        response = self.c.get(url_secret)
        j_only_secret = loads(response.content)
        self.assertEqual(j_only_secret['status'], 'Bad Request')

    @unittest.skip(u'Not Found? Server Error?')
    def test_about_invalid_key_and_secret(self):
        # TwitterのOAuth的に正しくないkey, secretの場合 Not Found
        j_not_found = sc.access_auth_view(self.c, 'key', 'secret')
        self.assertEqual(j_not_found['status'], 'Not Found')

    @unittest.skip(u'Not Found? Server Error?')
    def test_about_server_error(self):
        # key, secretによっては Server Error
        j_server_error = sc.access_auth_view(self.c, 'spam', 'egg')
        self.assertEqual(j_server_error['status'], 'Server Error')


class LectureTests(AuthenticatedTestCase):
    def setUp(self):
        # 授業を作成します
        self.make_auth_client()
        self.name = u'システム開発プロジェクト総合実験応用'
        self.code = u'76036'
        sc.access_lecture_add_view(self.c, self.name, self.code)

    def test_about_add_first_lecture(self):
        # 成功していれば、リストに1つだけ授業が存在する状態になります
        j_lec_get1 = sc.access_lecture_get_view(self.c)
        self.assertEqual(len(j_lec_get1['lectures']), 1)

    def test_about_add_existing_lecture(self):
        # 一度追加した授業は新規作成されません
        j_lec_add2 = sc.access_lecture_add_view(self.c, self.name, self.code)
        self.assertFalse(j_lec_add2['created'])

    def test_about_add_another_lecture(self):
        # 他の授業も追加してみます
        sc.access_lecture_add_view(
            self.c, name=u'オペレーティングシステム特論', code=u'76001')
        j_lec_get3 = sc.access_lecture_get_view(self.c)
        self.assertEqual(len(j_lec_get3['lectures']), 2)


class LectureFailTests(TestCase):
    def setUp(self):
        self.c = Client()
        self.name = u'コンピュータリテラシ'
        self.code = u'761'

    def test_about_lecture_add_only_name(self):
        # パラメタが間違っているとBad Request
        # NOTE: Bad Request の判定は Forbidden より先に行われる
        res_name = self.c.post('/api/lecture/add/', dict(name=self.name))
        j_add_only_name = loads(res_name.content)
        self.assertEqual(j_add_only_name['status'], 'Bad Request')

    def test_about_lecture_add_only_code(self):
        res_code = self.c.post('/api/lecture/add/', dict(code=self.code))
        j_add_only_code = loads(res_code.content)
        self.assertEqual(j_add_only_code['status'], 'Bad Request')

    def test_about_lecture_get_without_auth(self):
        # 認証しないでLecture get/add しようとするとForbidden
        j_get_fbd = sc.access_lecture_get_view(self.c)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_about_lecture_add_without_auth(self):
        j_add_fbd = sc.access_lecture_add_view(self.c, self.name, self.code)
        self.assertEqual(j_add_fbd['status'], 'Forbidden')


class TimeLineTests(AuthenticatedTestCase):
    # 準備: 授業データの読み込み
    fixtures = ['test_sample_lecture.json']

    def assertListSorted(self, *args):
        self.assertListEqual(sorted(args), list(args))

    def setUp(self):
        # TODO: テストをもっと簡素にする
        # (仮想タイムスタンプの振舞を調べるだけなら、もっと簡素にできるのでは?)
        self.make_auth_client()
        self.lec_id = sc.get_nth_lecture_dict(0)['id']

        # タイムラインに投稿 (ID, 実時間, 仮想時間を保存)
        self.j_post_new = sc.access_timeline_post_view(
            self.c, self.lec_id, body=u'しりとり')
        self.post0 = PostMetaDataFromJSON(self.j_post_new)

        # 先ほどの投稿より前に投稿する
        from time import sleep
        sleep(1)
        self.j_post_vts1 = sc.access_timeline_post_view(
            self.c, self.lec_id, body=u'逗子',
            before_virtual_ts=0, after_virtual_ts=self.post0.vts)
        self.post1 = PostMetaDataFromJSON(self.j_post_vts1)

        # この2つのpostの間に投稿
        sleep(1)
        self.j_post_vts2 = sc.access_timeline_post_view(
            self.c, self.lec_id, body=u'新橋',
            before_virtual_ts=self.post0.vts, after_virtual_ts=self.post1.vts)
        self.post2 = PostMetaDataFromJSON(self.j_post_vts2)

        # 仮想時間を指定しなければ最新の投稿になる
        sleep(1)
        self.j_post_vts3 = sc.access_timeline_post_view(
            self.c, self.lec_id, body=u'両国')
        self.post3 = PostMetaDataFromJSON(self.j_post_vts3)

    def test_about_real_time_comparison(self):
        # 実時間は減少することはない
        self.assertListSorted(self.post0.time,
                              self.post1.time,
                              self.post2.time,
                              self.post3.time)

    def test_about_vritual_time_stamp_comparison(self):
        # 仮想時間は指定した順に大きくなる
        self.assertListSorted(self.post1.vts,
                              self.post2.vts,
                              self.post0.vts,
                              self.post3.vts)

    def test_about_get_timeline(self):
        # 以上4つのpostがタイムラインに反映されているのでgetして確認
        j_timeline = sc.access_timeline_get_view(self.c, self.lec_id)
        self.posts = j_timeline['posts']

        self.assertEqual(len(self.posts), 4)
        self.assertEqual(self.posts[0]['id'], self.post1.pid)
        self.assertEqual(self.posts[1]['id'], self.post2.pid)
        self.assertEqual(self.posts[2]['id'], self.post0.pid)
        self.assertEqual(self.posts[3]['id'], self.post3.pid)


class TimeLineFailTests(TestCase):
    fixtures = ['test_sample_lecture.json']

    def setUp(self):
        self.c = Client()
        self.lec_id = sc.get_nth_lecture_dict(0)['id']

    def test_about_post_only_before_vts(self):
        # before_virtual_ts, after_virtual_ts 片方だけだと Bad Request
        j_only_before = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'投稿できないよ!', before_virtual_ts=65536)
        self.assertEqual(j_only_before['status'], 'Bad Request')

    def test_about_post_only_after_vts(self):
        j_only_after = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'投稿できないよ!', after_virtual_ts=65536)
        self.assertEqual(j_only_after['status'], 'Bad Request')

    def test_about_timeline_get_without_auth(self):
        # 認証しないで timeline get/post したら Forbidden
        j_get_fbd = sc.access_timeline_get_view(self.c, self.lec_id)
        self.assertEqual(j_get_fbd['status'], 'Forbidden')

    def test_about_timeline_post_without_auth(self):
        j_post_fbd = sc.access_timeline_post_view(self.c, self.lec_id,
            body=u'認証が切れたら、俺は投稿もできないのかよ')
        self.assertEqual(j_post_fbd['status'], 'Forbidden')

    def test_about_timeline__not_found(self):
        # 存在しない授業にget/postしようとすると Not Found
        # TODO: 書く場所を考え直す (このテストだけ認証が必要)
        sc.access_auth_view(self.c)
        dummy_lec_id = 42
        self.assertNotEqual(self.lec_id, dummy_lec_id)

        j_get_not = sc.access_timeline_get_view(self.c, dummy_lec_id)
        self.assertEqual(j_get_not['status'], 'Not Found')

        j_post_not = sc.access_timeline_post_view(self.c, dummy_lec_id,
            body=u'チャドの授業が・・・消えた・・・?')
        self.assertEqual(j_post_not['status'], 'Not Found')
