from django.db import models
from django.contrib.auth.models import User


class Question(models.Model):
    title = models.CharField(max_length=255)
    body = models.TextField()
    added_by = models.ForeignKey(User, null=True, blank=True)
    posted_at = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return u'[%s] %s' % (self.title, self.body)
