all:
	javac -d . src/User.java
	javac -d . src/Message.java
	javac -d . src/Rule.java
	javac -classpath lib/snakeyaml-1.11.jar:. -d . src/MessagePasser.java
	javac -d . src/Sender.java
	javac -d . src/ReceiverWorker.java
	javac -d . src/Receiver.java
	javac -classpath lib/snakeyaml-1.11.jar:. -d . src/lab0.java

clean:
	rm bin/*.class
