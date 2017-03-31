#!/bin/bash


echo "script initiated."

mkdir bin

javac -d bin -sourcepath src  src/cli/BackupClient.java  src/cli/BackupService.java

cd bin

x=1

start java cli.BackupService 1.0 "$2" teste  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224


while [ $x -le $1 ]
do
	if [ $x -ne $2 ] 
	then
		start java cli.BackupService 1.0 "$x" default  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224
	fi
		x=$(( $x + 1 ))
	
done
