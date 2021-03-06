#!/bin/bash
if [[ $# -ne 3 ]]; then
	echo "Usage: ./audioServer.sh k n songpath"
	echo "k : [1,...,n]. This server runs on ports 9030+k, 10030+k"
	echo "n : The number of servers"
	echo "songpath : File path to some delictably guilty pop music"
	exit 1;
fi
allServers=""
k=$1
n=$2
song=$3
for (( i=1 ; i<=($n-1) ; i++ )); do
	let c=9030+$i
	let s=10030+$i
	allServers+="localhost:$c:localhost:$s,"
done
#make sure last entry does not have the comma!!!!!
let c=9030+$n
let s=10030+$n
allServers+="localhost:$c:localhost:$s"
echo $allServers

#now calculate this server's address
let c=9030+$k
let s=10030+$k
me="localhost:$c:localhost:$s"

echo "------"
echo $song
echo $me
echo $allServers
echo "------"

#run!
java venturas/app/audio/MigAudioServer $song $me $allServers
