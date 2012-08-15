from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'mysite.views.home', name='home'),
    # url(r'^mysite/', include('mysite.foo.urls')),

    url(r'^api/auth/?$', 'mysite.question.views.auth_view'),
    # url(r'^api/get/?$', 'mysite.question.views.get_view'),
    # url(r'^api/post/?$', 'mysite.question.views.post_view'),
    url(r'^api/lecture/get/?$',
        'mysite.question.views.lecture_get_view'),
    url(r'^api/lecture/add/?$',
        'mysite.question.views.lecture_add_view'),
    url(r'^api/lecture/timeline/?$',
        'mysite.question.views.lecture_timeline_view'),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),
)
