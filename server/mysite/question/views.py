# -*- coding:utf-8 -*-

from django.http import HttpResponse,HttpResponseNotFound,HttpResponseForbidden
from django.shortcuts import render_to_response
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.models import User
import json
from mysite.question.models import Question

def convert_context_to_json(context):
    "Convert the context dictionary into a JSON object"
    # Note: This is *EXTREMELY* naive; in reality, you'll need
    # to do much more complex handling to ensure that arbitrary
    # objects -- such as Django model instances or querysets
    # -- can be serialized as JSON.
    return json.dumps(context)

def json_response(context={}):
    context['status']='OK'
    return HttpResponse(convert_context_to_json(context),mimetype='application/json')
def json_response_not_found(context={}):
    context['status']='Not Found'
    return HttpResponseNotFound(convert_context_to_json(context),mimetype='application/json')
def json_response_forbidden(context={}):
    context['status']='Forbidden'
    return HttpResponseForbidden(convert_context_to_json(context),mimetype='application/json')

def auth_view(request):
    if 'access_token' in request.GET:
        # old api version
        secret=request.GET['access_token']
        user_name=secret+'_name'
        try:
            user=User.objects.get(username=user_name)
            created=False
        except:
            user=User.objects.create_user(user_name,'',secret)
            created=True
            
        context=dict(created=created)
        return json_response(context)

    from twutil.tw_util import get_vc

    key=request.GET['access_token_key']    
    secret=request.GET['access_token_secret']
    vc=get_vc(key,secret)
    if vc=={}:
        return json_response_not_found()
    user_name=vc['id']
    try:
        user=User.objects.get(username=user_name)
        created=False
    except:
        user=User.objects.create_user(user_name,'',secret)
        created=True

    context=dict(created=created)
    return json_response(context)

def get_view(request):
    posts=[dict(title=q.title, body=q.body) for q in Question.objects.all()]
    context=dict(
       posts=posts
    )
    return json_response(context) 

@csrf_exempt
def post_view(request):
    title=request.POST['title']
    body=request.POST['body']
    added_by=request.user
    if added_by.is_authenticated():
        Question.objects.create(title=title, body=body, added_by=added_by)
        return json_response()
    else:
        return json_response_forbidden()
