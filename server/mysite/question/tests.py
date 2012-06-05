"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.

Test about [auth_view] function
>>> from django.test.client import Client
>>> import json
>>> c=Client(enforce_csrf_checks=True)
>>> response=c.get('/api/auth/',dict(access_token='ACCESS_TOKEN'))
>>> response.status_code
200
>>> jsonobj=json.loads(response.content)
>>> 'status' in jsonobj
True



Test about [get_view] function
>>> from django.test.client import Client
>>> import json
>>> c=Client(enforce_csrf_checks=True)
>>> response=c.get('/api/get/')
>>> response.status_code
200
>>> jsonobj=json.loads(response.content)
>>> 'status' in jsonobj
True
>>> 'posts' in jsonobj
True



Test about [post_view] function
>>> from django.test.client import Client
>>> c=Client()
>>> response=c.post('/api/post/',dict(title='csrf test2',body='this pass'))
>>> response.status_code
200

If don't use @csrf_exempt decorator, this test Failed
because response.status_code Got 403, while above test is OK.
So, use csrf check client
>>> c=Client(enforce_csrf_checks=True)
>>> response=c.post('/api/post/',dict(title='csrf test2',body='this pass'))
>>> response.status_code
200
"""

from django.test import TestCase


class SimpleTest(TestCase):
    def test_basic_addition(self):
        """
        Tests that 1 + 1 always equals 2.
        """
        self.assertEqual(1 + 1, 2)
