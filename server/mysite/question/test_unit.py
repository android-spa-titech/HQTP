# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
from django.test import TestCase
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


class AuthenticateTests(TestCase):
    def setUp(self):
        # first log-in user
        c = sc.make_client()
        self.j_auth = sc.access_auth_view(c)
        self.user = self.j_auth['user']

        # second log-in user
        c2 = sc.make_client()
        self.j_auth2 = sc.access_auth_view(c2)

    def test_about_user_info(self):
        self.assertEqual(self.j_auth['status'], 'OK')
        self.assertIn('created', self.j_auth)
        self.assertTrue(self.j_auth['created'])
        self.assertIn('id', self.user)
        self.assertIn('name', self.user)
        self.assertIn('icon_url', self.user)
        self.assertIn('api.twitter.com/1/users/profile_image',
                      self.user['icon_url'])
        self.assertIn(self.user['name'], self.user['icon_url'])

    def test_about_second_auth(self):
        self.assertEqual(self.j_auth2['status'], 'OK')
        self.assertFalse(self.j_auth2['created'])


class AuthenticateFailTests(TestCase):
    def setUp(self):
        from mysite.question.twutil.consumer_info import spa_key, spa_secret
        c = sc.make_client()

        # access_token_key しか渡されていないと Bad Request
        url_key = '/api/auth/?access_token_key=%s'
        url_key = url_key % spa_key
        response = c.get(url_key)
        self.j_only_key = loads(response.content)

        # access_token_secret しか渡されていないと Bad Request
        url_secret = '/api/auth/?access_token_secret=%s'
        url_secret = url_secret % spa_secret
        response = c.get(url_secret)
        self.j_only_secret = loads(response.content)

        # TwitterのOAuth的に正しくないkey, secretの場合 Not Found
        self.j_not_found = sc.access_auth_view(c, 'dummy key', 'dummy secret')

        # key, secretによっては Server Error
        self.j_server_error = sc.access_auth_view(c, 'spam', 'egg')

    def test_about_error_status(self):
        self.assertEqual(self.j_only_key['status'], 'Bad Request')
        self.assertEqual(self.j_only_secret['status'], 'Bad Request')
        self.assertEqual(self.j_not_found['status'], 'Not Found')
        self.assertEqual(self.j_server_error['status'], 'Server Error')


class LectureTests(TestCase):
    def setUp(self):
        # 認証します
        c = sc.make_client()
        self.j_auth = sc.access_auth_view(c)

        # 最初は何も授業がありません
        self.j_lec_get0 = sc.access_lecture_get_view(c)

        # 授業を追加し、IDを保存
        name = u'システム開発プロジェクト総合実験応用'
        code = u'76036'
        self.lec = sc.access_lecture_add_view(c, name, code)
        self.lec_id = self.lec['lecture']['id']

        # 授業一覧を取得すると反映されています
        self.j_lec_get1 = sc.access_lecture_get_view(c)
        self.lectures = self.j_lec_get1['lectures']

        # 一度追加した授業は新規作成されません
        self.lec_2nd = sc.access_lecture_add_view(c, name, code)
        self.j_lec_get2 = sc.access_lecture_get_view(c)

        # 他の授業も追加してみます
        self.j_lec_add3 = sc.access_lecture_add_view(
            c, name=u'オペレーティングシステム特論', code=u'76001')
        self.j_lec_get3 = sc.access_lecture_get_view(c)

    def test_about_authenticate(self):
        self.assertEqual(self.j_auth['status'], 'OK')

    def test_about_get_empty_lecture_list(self):
        self.assertEqual(self.j_lec_get0['status'], 'OK')
        self.assertListEqual(self.j_lec_get0['lectures'], [])

    def test_about_add_first_lecture(self):
        self.assertEqual(self.lec['status'], 'OK')
        self.assertTrue(self.lec['created'])

    def test_about_get_one_lecture(self):
        self.assertEqual(self.j_lec_get1['status'], 'OK')
        self.assertEqual(len(self.lectures), 1)
        self.assertEqual(self.lectures[0]['code'], self.lec['lecture']['code'])
        self.assertEqual(self.lectures[0]['name'], self.lec['lecture']['name'])
        self.assertEqual(self.lectures[0]['id'], self.lec_id)

    def test_about_add_existing_lecture(self):
        self.assertFalse(self.lec_2nd['created'])
        self.assertEqual(len(self.j_lec_get2['lectures']), 1)

    def test_about_add_another_lecture(self):
        self.assertTrue(self.j_lec_add3['created'])
        self.assertEqual(len(self.j_lec_get3['lectures']), 2)


class LectureFailTests(TestCase):
    def setUp(self):
        c = sc.make_client()
        name = u'コンピュータリテラシ'
        code = u'761'

        # パラメタが間違っているとBad Request
        # NOTE: Bad Request の判定は Forbidden より先に行われる
        url = '/api/lecture/add/'
        res_name = c.post(url, dict(name=name))
        self.j_add_only_name = loads(res_name.content)
        res_code = c.post(url, dict(code=code))
        self.j_add_only_code = loads(res_code.content)

        # 認証しないでLecture get/add しようとするとForbidden
        self.j_get_fbd = sc.access_lecture_get_view(c)
        self.j_add_fbd = sc.access_lecture_add_view(c, name, code)

        # 認証すると以上の操作が可能になります
        self.j_auth = sc.access_auth_view(c)
        self.j_get_auth = sc.access_lecture_get_view(c)
        self.j_add_auth = sc.access_lecture_add_view(c, name, code)

    def test_about_lecture_add__bad_request(self):
        self.assertEqual(self.j_add_only_name['status'], 'Bad Request')
        self.assertEqual(self.j_add_only_code['status'], 'Bad Request')

    def test_about_lecture__forbidden(self):
        self.assertEqual(self.j_get_fbd['status'], 'Forbidden')
        self.assertEqual(self.j_add_fbd['status'], 'Forbidden')

    def test_about_authenticated_user(self):
        self.assertEqual(self.j_auth['status'], 'OK')
        self.assertEqual(self.j_get_auth['status'], 'OK')
        self.assertEqual(self.j_add_auth['status'], 'OK')


class TimeLineTests(TestCase):
    # 準備: 授業データの読み込み
    fixtures = ['test_sample_lecture.json']

    def assertListSorted(self, *args):
        self.assertListEqual(sorted(args), list(args))

    def setUp(self):
        # 準備: ユーザーの認証と授業IDの取得
        c = sc.make_client()
        self.j_auth = sc.access_auth_view(c)
        lec_id = sc.get_nth_lecture_dict(0)['id']

        # 最初はなにも投稿がありません
        self.j_tl = sc.access_timeline_get_view(c, lec_id)

        # タイムラインに投稿
        self.j_post_new = sc.access_timeline_post_view(
            c, lec_id, body=u'しりとり')

        # あとで使うのでID, 実時間, 仮想時間を保存
        self.post0 = PostMetaDataFromJSON(self.j_post_new)

        # 先ほどの投稿より前に投稿する
        from time import sleep
        sleep(1)
        self.j_post_vts1 = sc.access_timeline_post_view(
            c, lec_id, body=u'逗子',
            before_virtual_ts=0, after_virtual_ts=self.post0.vts)
        self.post1 = PostMetaDataFromJSON(self.j_post_vts1)

        # この2つのpostの間に投稿
        sleep(1)
        self.j_post_vts2 = sc.access_timeline_post_view(
            c, lec_id, body=u'新橋',
            before_virtual_ts=self.post0.vts, after_virtual_ts=self.post1.vts)
        self.post2 = PostMetaDataFromJSON(self.j_post_vts2)

        # 仮想時間を指定しなければ最新の投稿になる
        sleep(1)
        self.j_post_vts3 = sc.access_timeline_post_view(
            c, lec_id, body=u'両国')
        self.post3 = PostMetaDataFromJSON(self.j_post_vts3)

        # 以上4つのpostがタイムラインに反映されているのでgetして確認
        self.j_tl2 = sc.access_timeline_get_view(c, lec_id)
        self.posts = self.j_tl2['posts']

    def test_about_authenticate(self):
        self.assertEqual(self.j_auth['status'], 'OK')

    def test_about_get_empty_timeline(self):
        self.assertEqual(self.j_tl['status'], 'OK')
        self.assertListEqual(self.j_tl['posts'], [])

    def test_about_post_to_timeline_status(self):
        self.assertEqual(self.j_post_new['status'], 'OK')
        self.assertEqual(self.j_post_vts1['status'], 'OK')
        self.assertEqual(self.j_post_vts2['status'], 'OK')
        self.assertEqual(self.j_post_vts3['status'], 'OK')

    def test_about_real_time_comparison(self):
        # 実時間は減少することはない
        self.assertLess(self.post0.time, self.post1.time)
        self.assertListSorted(self.post0.time,
                              self.post1.time,
                              self.post2.time)
        self.assertListSorted(self.post0.time,
                              self.post1.time,
                              self.post2.time,
                              self.post3.time)

    def test_about_vritual_time_stamp_comparison(self):
        # 仮想時間は小さくなっている post0_vts > post1_vts
        self.assertGreater(self.post0.vts, self.post1.vts)
        # 仮想時間は中間値になっている
        self.assertListSorted(self.post1.vts,
                              self.post2.vts,
                              self.post0.vts)
        # 仮想時間は最大になっている
        self.assertListSorted(self.post1.vts,
                              self.post2.vts,
                              self.post0.vts,
                              self.post3.vts)

    def test_about_get_timeline(self):
        self.assertEqual(self.j_tl2['status'], 'OK')
        self.assertEqual(len(self.posts), 4)
        self.assertEqual(self.posts[0]['id'], self.post1.pid)
        self.assertEqual(self.posts[1]['id'], self.post2.pid)
        self.assertEqual(self.posts[2]['id'], self.post0.pid)
        self.assertEqual(self.posts[3]['id'], self.post3.pid)


class TimeLineFailTests(TestCase):
    fixtures = ['test_sample_lecture.json']

    def setUp(self):
        c = sc.make_client()
        lec_id = sc.get_nth_lecture_dict(0)['id']

        # before_virtual_ts, after_virtual_ts 片方だけだと Bad Request
        self.j_only_before = sc.access_timeline_post_view(c, lec_id,
            body=u'投稿できないよ!', before_virtual_ts=65536)
        self.j_only_after = sc.access_timeline_post_view(c, lec_id,
            body=u'投稿できないよ!', after_virtual_ts=65536)

        # 認証しないで timeline get/post したら Forbidden
        self.j_get_fbd = sc.access_timeline_get_view(c, lec_id)
        self.j_post_fbd = sc.access_timeline_post_view(c, lec_id,
            body=u'認証が切れたら、俺は投稿もできないのかよ')

        # 認証すると get/post できます
        self.j_auth = sc.access_auth_view(c)
        self.j_get_auth = sc.access_timeline_get_view(c, lec_id)
        self.j_post_auth = sc.access_timeline_post_view(c, lec_id,
            body=u'計　　画　　通　　り')

        # 存在しない授業にget/postしようとすると Not Found
        self.j_get_not = sc.access_timeline_get_view(c, lec_id + 1)
        self.j_post_not = sc.access_timeline_post_view(c, lec_id + 1,
            body=u'チャドの授業が・・・消えた・・・?')

    def test_about_post__bad_request(self):
        self.assertEqual(self.j_only_before['status'], 'Bad Request')
        self.assertEqual(self.j_only_after['status'], 'Bad Request')

    def test_about_timeline__forbidden(self):
        self.assertEqual(self.j_get_fbd['status'], 'Forbidden')
        self.assertEqual(self.j_post_fbd['status'], 'Forbidden')

    def test_about_authenticated_user(self):
        self.assertEqual(self.j_auth['status'], 'OK')
        self.assertEqual(self.j_get_auth['status'], 'OK')
        self.assertEqual(self.j_post_auth['status'], 'OK')

    def test_about_timeline__not_found(self):
        self.assertEqual(self.j_get_not['status'], 'Not Found')
        self.assertEqual(self.j_post_not['status'], 'Not Found')
