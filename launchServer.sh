#!/bin/bash

cd bin

x=$1


while [ $x -le $2 ]
do
	gnome-terminal -x bash -c "java cli.BackupService 1.0 $x $x 224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224; exec bash"
	x=$(( $x + 1 ))

done

