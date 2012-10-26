# -*- coding:utf-8 -*-

"""
静的ファイルに関するユーティリティ群
"""

import urllib
from os import path
from django.conf import settings


def get_img(url):
    """
    URLから画像データを取得
    imageファイルでない場合はNoneを返す
    """
    f = urllib.urlopen(url)
    file_type = f.info().gettype()
    src = f.read()  # source string (binary)
    f.close()
    if 'image' in file_type:  # not error page
        return src
    else:
        return None


def save_bindata(absolute_pathname, data):
    """
    絶対パスにバイナリデータを保存
    """
    out = open(absolute_pathname, 'wb')
    out.write(data)
    out.close()


def build_media_absolute_pathname(relative_pathname):
    """
    ローカルの相対パスにMEDIA_ROOTをつけてローカルの絶対パスにする
    """
    return path.join(settings.MEDIA_ROOT, relative_pathname)


def build_media_absolute_url(request, relative_pathname):
    """
    ローカルの相対パスからURLの絶対パスにする
    """
    relative_url = urllib.pathname2url(relative_pathname)
    # note: requestからprotocol,domain,portを考慮して完全URLを生成する
    return request.build_absolute_uri('/site_media/' + relative_url)
