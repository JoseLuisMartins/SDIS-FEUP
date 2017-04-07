#!/bin/bash


mkdir -p bin

javac -d bin -sourcepath src -Xlint $(find . -name \*.java)



