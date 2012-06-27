from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save


class Question(models.Model):
    title = models.CharField(max_length=255)
    body = models.TextField()
    added_by = models.ForeignKey(User, null=True, blank=True)
    posted_at = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return u'[%s] %s' % (self.title, self.body)

    def to_dict(self):
        return dict(id=self.pk,
                    title=self.title,
                    body=self.body,
                    user=user_to_dict(self.added_by),
                    time=self.posted_at.isoformat())


class UserProfile(models.Model):
    user = models.OneToOneField(User)

    # Additional information about User
    screen_name = models.CharField(max_length=255)
    name = models.CharField(max_length=255)


def user_to_dict(user):
    return dict(id=user.pk,
                name=user.username)


def create_user_profile(sender, instance, created, **kwargs):
    """
    if user is created (or changed),
    then create user profile about the user, using signal

    kwargs contains such key 'raw', 'signal', 'using'
    """

    if created:
        # user is created, not changed
        UserProfile.objects.create(user=instance)

post_save.connect(create_user_profile, sender=User)
