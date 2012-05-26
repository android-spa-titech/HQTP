# Create your views here.
from django.http import HttpResponse
from django.shortcuts import render_to_response

def auth_view(request):
    access_token=request.GET['access_token']
    dic={'access_token':access_token}
    return render_to_response('question/auth.html',dic)

def get_view(request):
    return HttpResponse("Welcome to HQTP ! [get]") 

def post_view(request):
    title=request.POST['title']
    body=request.POST['body']
    return HttpResponse("Welcome to HQTP ! [post] title=%s, body=%s" % (title, body))
