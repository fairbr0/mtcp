==========RUNNING/COMPILING THIS WORK==========
./all.sh
	Compiles all files and runs both client and server apps if successful.

./buildAll.sh
	Compiles all files but does not run applications.

./client.sh
	Does no compilation work, but will run alrrady compiled client app.

./server.sh
        Does no compilation work, but will run alrrady compiled server app.

./runTest.sh
		Runs a unit test. Give it the package name, followed by test class, eg io.SerializerTest

===========PACKAGE DISCUSSION============
venturas

	.mtcp
		.sockets
			includes migratory sockets (both client & server side)
		.io
			includes emulated i/o streams for mig sockets
		.packets
			includes packet class and flags (should NOT be used by apps)
	.app
		includes client and server music streaming applications
