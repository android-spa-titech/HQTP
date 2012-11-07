#!/usr/bin/expect

set timeout 10
spawn ./manage.py shell

expect ">>>"
send "from django.test.client import Client\n"

expect ">>>"
send "c = Client()\n"

expect ">>>"
send "sc.access_auth_view(c)\n"

interact

