# -*- coding:utf-8 -*-
"""
標準のsettings.pyの設定のうち
データベースに関してFixture用の設定を上書きする
"""

import settings
dbname = 'fixture_db.sqlite'

locals().update(settings.__dict__)
locals()['DATABASES']['default']['ENGINE'] = 'django.db.backends.sqlite3'
locals()['DATABASES']['default']['NAME'] = dbname
