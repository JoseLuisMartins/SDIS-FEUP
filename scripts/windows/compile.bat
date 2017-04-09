@echo off

cd ..
cd ..

mkdir bin

javac -d bin -sourcepath src src/cli/BackupService.java src/cli/BackupClient.java
