# -*- coding: utf-8 -*-

import mysite.question.shortcuts as sc
import unittest


class PostTestHelper:
    def __init__(self, jobj):
        """
        post = jobj['post']
        post1_id = post['id']
        post1_time = post['time']
        post1_vts = post['virtual_ts']
        """
        self.pid = jobj['post']['id']
        self.time = jobj['post']['time']
        self.vts = jobj['post']['virtual_ts']


class HQTPTestCase(unittest.TestCase):
    # test_viewsにあるすべてのテストを移植するわけではない
    # ただし、こちらの方がいろいろと便利なのでいくつか完全に移植したい
    def setUp(self):
        self.c = sc.make_client()
        self.j_auth = sc.access_auth_view(self.c)
        self.lec_name1 = u'ソフトウェアテスト演習'
        self.lec_code1 = '76050'

    def assertListLess(self, *args):
        arg_prev = args[0]
        for arg_next in args[1:]:
            self.assertLess(arg_prev, arg_next)
            arg_prev = arg_next

    def test_about_auth(self):
        user = self.j_auth['user']

        # test about user info
        self.assertEqual(self.j_auth['status'], 'OK')
        self.assertIn('created', self.j_auth)
        self.assertIn('id', user)
        self.assertIn('name', user)
        self.assertIn('icon_url', user)
        self.assertIn('api.twitter.com/1/users/profile_image',
                      user['icon_url'])
        self.assertIn(user['name'], user['icon_url'])

        # user is created only first time
        c2 = sc.make_client()
        jobj2 = sc.access_auth_view(c2)
        self.assertEqual(jobj2['status'], 'OK')
        self.assertFalse(jobj2['created'])

    def test_about_lecture(self):
        # 最初は何も授業がありません
        j_lec_get = sc.access_lecture_get_view(self.c)
        self.assertEqual(j_lec_get['status'], 'OK')
        self.assertListEqual(j_lec_get['lectures'], [])

        # 授業を追加します
        j_lec_add = sc.access_lecture_add_view(self.c, self.lec_name1,
                                               self.lec_code1)
        self.assertEqual(j_lec_add['status'], 'OK')
        self.assertTrue(j_lec_add['created'])

        # あとで使うのでIDを保存
        res_lec_id1 = j_lec_add['lecture']['id']

        # lecture/getで確認
        j_lec_get1 = sc.access_lecture_get_view(self.c)
        lectures = j_lec_get1['lectures']

        self.assertEqual(j_lec_get1['status'], 'OK')
        self.assertEqual(len(lectures), 1)
        self.assertEqual(lectures[0]['code'], self.lec_code1)
        self.assertEqual(lectures[0]['name'], self.lec_name1)
        self.assertEqual(lectures[0]['id'], res_lec_id1)

        # 2度目の追加は新規作成されません
        j_lec_add2 = sc.access_lecture_add_view(self.c, self.lec_name1,
                                                self.lec_code1)
        j_lec_get2 = sc.access_lecture_get_view(self.c)

        self.assertFalse(j_lec_add2['created'])
        self.assertEqual(len(j_lec_get2['lectures']), 1)

        # 他の授業も追加してみる
        j_lec_add3 = sc.access_lecture_add_view(self.c, 'Arch', '76001')
        j_lec_get3 = sc.access_lecture_get_view(self.c)

        self.assertTrue(j_lec_add3['created'])
        self.assertEqual(len(j_lec_get3['lectures']), 2)

    def test_about_timeline(self):
        # 準備 (授業の作成)
        j_lec = sc.access_lecture_add_view(self.c, self.lec_name1,
                                           self.lec_name1)
        res_lec_id = j_lec['lecture']['id']

        # ユーザーを認証します
        c1 = sc.make_client()
        j_auth = sc.access_auth_view(c1)
        self.assertEqual(j_auth['status'], 'OK')

        # 最初はなにも投稿がありません
        j_tl = sc.access_timeline_get_view(c1, res_lec_id)
        self.assertEqual(j_tl['status'], 'OK')
        self.assertListEqual(j_tl['posts'], [])

        # タイムラインに投稿
        j_post_new = sc.access_timeline_post_view(
            c1, res_lec_id, body=u'遅刻した',)
        self.assertEqual(j_post_new['status'], 'OK')

        # あとで使うのでID, 実時間, 仮想時間を保存
        post0 = PostTestHelper(j_post_new)

        # --------------------------------------------------------------------
        # 先ほどの投稿より前に投稿する
        from time import sleep
        sleep(3)
        j_post_vts1 = sc.access_timeline_post_view(
            c1, res_lec_id, body=u'難しい',
            before_virtual_ts=0, after_virtual_ts=post0.vts)
        self.assertEqual(j_post_vts1['status'], 'OK')

        post1 = PostTestHelper(j_post_vts1)

        # 実時間が減少することはない post0_time < post1_time
        self.assertLess(post0.time, post1.time)

        # 仮想時間は小さくなっている post0_vts > post1_vts
        self.assertGreater(post0.vts, post1.vts)

        # --------------------------------------------------------------------
        # この2つのpostの間に投稿
        sleep(3)
        j_post_vts2 = sc.access_timeline_post_view(
            c1, res_lec_id, body=u'分からん',
            before_virtual_ts=post0.vts, after_virtual_ts=post1.vts)
        self.assertEqual(j_post_vts2['status'], 'OK')

        post2 = PostTestHelper(j_post_vts2)

        # 実時間が減少することはない
        self.assertListLess(post0.time, post1.time, post2.time)

        # 仮想時間は中間値になっている
        self.assertListLess(post1.vts, post2.vts, post0.vts)

        # --------------------------------------------------------------------
        # 仮想時間を指定しなければ最新の投稿になる
        sleep(3)
        j_post_vts3 = sc.access_timeline_post_view(
            c1, res_lec_id, body=u'眠い')
        self.assertEqual(j_post_vts3['status'], 'OK')

        post3 = PostTestHelper(j_post_vts3)

        # 実時間は減少しない
        self.assertListLess(post0.time, post1.time, post2.time, post3.time)

        # 仮想時間は最大になっている
        self.assertListLess(post1.vts, post2.vts, post0.vts, post3.vts)

        # --------------------------------------------------------------------
        # 以上4つのpostがタイムラインに反映されているのでgetして確認
        j_tl2 = sc.access_timeline_get_view(c1, res_lec_id)
        posts = j_tl2['posts']

        self.assertEqual(j_tl2['status'], 'OK')
        self.assertEqual(len(posts), 4)
        self.assertEqual(posts[0]['id'], post1.pid)
        self.assertEqual(posts[1]['id'], post2.pid)
        self.assertEqual(posts[2]['id'], post0.pid)
        self.assertEqual(posts[3]['id'], post3.pid)


def test_suite():
    suite = unittest.TestSuite()
    suite.addTest(HQTPTestCase('test_about_auth'))
    suite.addTest(HQTPTestCase('test_about_lecture'))
    suite.addTest(HQTPTestCase('test_about_timeline'))
    return suite
