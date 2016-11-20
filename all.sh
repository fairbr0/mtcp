#!/bin/bash

pkill -9 java

./buildAll.sh
if [[ $? -ne 1 ]]; then
	./server.sh &
	./client.sh &
	exit 0;
else
	exit 1;
fi
