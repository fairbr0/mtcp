#!/bin/bash

pkill -9 java

#Default behaviour provides no flags to the javac compiler
flags=""

if [[ $1 == "-w" ]]; then
	# Compile with warnings:
	flags="-Xlint:unchecked"
fi

for package in "mtcp/packets" "mtcp/sockets" "mtcp/io" "app" "app/audio" "test/mtcp/io" "test/mtcp/sockets"; do
	javac -cp ".:venturas:libraries/junit-4.12.jar:libraries/hamcrest-core-1.3.jar" $flags venturas/$package/*.java
	if [[ $? -ne 0 ]]; then
		echo "💔  COMPILE ERROR on package venturas/$package, see error message. Script will terminate.";
		exit 1;
	fi
done

echo "💙  COMPILE SUCCESS, add -w flag to include compile warnings";

exit 0;
