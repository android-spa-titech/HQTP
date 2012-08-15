from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save


class Lecture(models.Model):
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=255)

    def __unicode__(self):
        return u'[%s] %s' % (self.code, self.name)

    def to_dict(self):
        return dict(id=self.pk,
                    name=self.name,
                    code=self.code)


class Question(models.Model):
    body = models.TextField()
    added_by = models.ForeignKey(User, null=True, blank=True)
    posted_at = models.DateTimeField(auto_now_add=True)
    lecture = models.ForeignKey(Lecture, null=True, blank=True)
    virtual_ts = models.BigIntegerField()

    def __unicode__(self):
        return self.body

    def to_dict(self):
        return dict(id=self.pk,
                    body=self.body,
                    user=user_to_dict(self.added_by),
                    time=self.posted_at.isoformat(),
                    lecture=self.lecture.to_dict(),
                    virtual_ts=self.virtual_ts)


class UserProfile(models.Model):
    user = models.OneToOneField(User)

    # Additional information about User
    screen_name = models.CharField(max_length=255)
    name = models.CharField(max_length=255)
    icon_url = models.CharField(max_length=255)


def user_to_dict(user):
    return dict(id=user.pk,
                name=user.get_profile().screen_name,
                icon_url=user.get_profile().icon_url)


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
