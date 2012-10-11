import mysite.question.shortcuts as shortcuts
import unittest


class HQTPTestCase(unittest.TestCase):
    def test_about_api_auth(self):
        # prepare
        c = shortcuts.make_client()
        jobj = shortcuts.access_auth_view(c)
        user = jobj['user']

        # test about authenticate
        self.failUnlessEqual(jobj['status'], 'OK')
        # never mind if '' or u''
        self.failUnlessEqual(jobj['status'], u'OK')
        # miss test
        # self.failUnlessEqual(jobj['status'], 'KO')

        self.assertTrue('created' in jobj)
        self.assertTrue('id' in user)
        self.assertTrue('name' in user)
        self.assertTrue('icon_url' in user)


def test_suite():
    return unittest.TestLoader().loadTestsFromTestCase(HQTPTestCase)
