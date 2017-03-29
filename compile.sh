#!/bin/bash


echo "script initiated."

mkdir bin

javac -d bin -sourcepath src  src/cli/BackupClient.java  src/cli/BackupService.java

cd bin

x=1

start java cli.BackupService 1.0 "$x" teste  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224

x=$(( $x + 1 ))

while [ $x -le $1 ]
do
	start java cli.BackupService 1.0 "$x" default  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224

	x=$(( $x + 1 ))
	
done
