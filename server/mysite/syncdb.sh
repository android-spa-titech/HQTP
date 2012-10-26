#!/usr/bin/expect

set timeout 10
spawn rm mydb.sqlite
spawn python manage.py syncdb
expect "(yes/no)"
send "yes\n"
expect "Username"
send "test\n"
expect "E-mail address"
send "test@example.com\n"
expect "Password"
send "test\n"
expect "Password (again)"
send "test\n"
interact

