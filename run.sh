#!/bin/bash

test -z $CP && export CP=`mvn dependency:build-classpath | grep '^/'`

java -cp $CP:target/classes org.mofleury.agwenst.App
