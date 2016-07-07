rk : *.java
	javac -classpath .:classes:/opt/pi4j/lib/'*' -d /home/pi/project/comp /home/pi/project/GLock/src/GLock/*.java

run : 
	java -classpath classes:/opt/pi4j/lib/'*':/home/pi/project/comp GLock.GLock
