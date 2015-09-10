#!/bin/bash
wget http://www.jeffeppinger.com/GenericDAO/genericdao-2.0.2.jar
mvn install:install-file -Dfile=genericdao-2.0.2.jar -DgroupId=com.jeffeppinger -DartifactId=genericdao -Dversion=2.0.2 -Dpackaging=jar
rm genericdao-2.0.2.jar
