#!/bin/bash


mkdir -p bin

javac -d bin -sourcepath src $(find . -name \*.java)



