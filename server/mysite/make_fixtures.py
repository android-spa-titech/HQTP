# -*- coding:utf-8 -*-
"""
DjangoのTest Fixtureを用意するプログラム
python make_fixtures.pyで作成
ダンプ作業はディレクトリが無ければ新規作成を行う。
ダンプ先のファイルは自動的に上書きされる。
"""
import os
import sys
import subprocess


prefix = 'fixture_'  # Fixtureを作成する関数はprefixから始まる
dbname = 'mydb.sqlite'  # TODO: Fixture作業用DBを指定できるようにする


def reset_database(appname):
    """
    appnameに関連するテーブルをリセットする
    init_databaseより断然速い
    """

    # Are you sure you want to reset database?
    # Type 'yes' to continue, or 'no' to cancel:
    p_echo = subprocess.Popen(['echo', 'yes'], stdout=subprocess.PIPE)
    p = subprocess.Popen(['python', 'manage.py', 'reset', appname],
                         stdin=p_echo.stdout)
    p.communicate()


def sync_database():
    """
    作業用データベースを初期化するためにsyncdbを行う
    """
    # Would you like to create superusers now? (yes/no):
    p_echo = subprocess.Popen(['echo', 'no'], stdout=subprocess.PIPE)
    p = subprocess.Popen(['python', 'manage.py', 'syncdb'],
                         stdin=p_echo.stdout)
    p.communicate()


def dump_database(pathname):
    """
    pathnameにデータベースのダンプを行う
    """
    p = subprocess.Popen(['python', 'manage.py', 'dumpdata'],
                         stdout=subprocess.PIPE)
    out, err = p.communicate()

    # directory exists?
    dirname = os.path.dirname(pathname)
    if not os.path.isdir(dirname):
        if os.path.exists(dirname):
            sys.stderr.write(dirname + ' is not directory')
            sys.exit(1)
        os.makedirs(dirname)

    f = open(pathname, 'w')
    f.write(out)
    f.close()


def decofixture(appname):
    """
    終了処理として、ダンプとリセットを行うデコレータ
    ダンプするファイルは./appname/fixtures/{関数名-prefix}.json
    """
    def receive_func(func):
        import functools

        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            errmsg = 'function name must starts with ' + prefix
            assert func.__name__.startswith(prefix), errmsg

            filename = func.__name__[len(prefix):] + '.json'
            pathname = os.path.join(appname, 'fixtures', filename)

            func(*args, **kwargs)
            dump_database(pathname)
            reset_database(appname)
        return wrapper
    return receive_func


###############################################################################
# define your fixture_ funtions
###############################################################################
@decofixture('question')
def fixture_lecture():
    from question.models import Lecture
    Lecture.objects.create(name='ITSP', code='12345')


###############################################################################
if __name__ == '__main__':
    os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

    # DBを初期化
    os.remove(dbname)
    sync_database()

    # call fixture_ functions
    fs = filter(lambda (k, v): k.startswith(prefix), locals().items())
    for k, func in fs:
        func()
