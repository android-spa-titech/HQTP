# Create your views here.
from django.http import HttpResponse

def auth_view(request):
    access_token=request.GET['access_token']
    return HttpResponse("Welcome to HQTP ! [auth] access_token=%s" % access_token)

def get_view(request):
    return HttpResponse("Welcome to HQTP ! [get]") 

def post_view(request):
    title=request.POST['title']
    body=request.POST['body']
    return HttpResponse("Welcome to HQTP ! [post] title=%s, body=%s" % (title, body))
