#!/bin/sh

if [ "$#" -eq 1 ]; then
echo "server $1
update delete default
send
quit
" | nsupdate -t 1 -k /etc/rndc.key

elif [ "$#" -eq 5 ]; then

echo "server $5
delete $1
update add $1 $4 IN $3 $2
send
quit
" | nsupdate -t 1 -k /etc/rndc.key
fi
