@echo off
 
SET /p servers=numero de servers :
SET /a max=servers 

javac -d bin -sourcepath src  src/cli/BackupClient.java  src/cli/BackupService.java

cd bin

FOR /L %%a IN (1,1,%max%) DO ( start java cli.BackupService 1.0 %%a %%a 224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224)


