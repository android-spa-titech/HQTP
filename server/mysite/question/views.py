# -*- coding:utf-8 -*-

from django.http import HttpResponse,HttpResponseNotFound
from django.shortcuts import render_to_response
import json

def convert_context_to_json(context):
    "Convert the context dictionary into a JSON object"
    # Note: This is *EXTREMELY* naive; in reality, you'll need
    # to do much more complex handling to ensure that arbitrary
    # objects -- such as Django model instances or querysets
    # -- can be serialized as JSON.
    return json.dumps(context)

def json_response(context):
    return HttpResponse(convert_context_to_json(context),mimetype='application/json')
def json_response_not_found(context):
    return HttpResponseNotFound(convert_context_to_json(context),mimetype='application/json')

def auth_view(request):
    access_token=request.GET['access_token']
    # TODO auth

    context={'status':'OK'}
    return json_response(context)

def get_view(request):
    # TODO load questions from database

    context=dict(
        status='ok'
       ,posts=[
            dict(title=u'課題',body=u'今日の課題の範囲が分からない')
           ,dict(title='NFA?',body=u'NFAってなんの略だっけ？')
           ,dict(title=u'状態数orz',body=u'決定化してるんだけど、状態めちゃ増える。人間にやらせるなんて')
        ]
    )
    return json_response(context) 

def post_view(request):
    title=request.POST['title']
    body=request.POST['body']
    # TODO save this post

    context={'status':'OK'}
    return json_response(context)
