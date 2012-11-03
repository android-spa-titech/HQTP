#!/usr/bin/expect

set timeout 10
spawn ./manage.py shell

expect ">>>"
send "import mysite.question.shortcuts as sc\n"

expect ">>>"
send "c = sc.make_client()\n"

expect ">>>"
send "sc.access_auth_view(c)\n"

interact

