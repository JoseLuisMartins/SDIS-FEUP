#!/bin/bash

cd ../..
mkdir -p bin

javac -d bin -sourcepath src -Xlint $(find . -name \*.java)
