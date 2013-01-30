all:
	rm bin/*.class
	javac -d . src/User.java
	javac -d . src/Rule.java
	javac -d . src/Message.java
	javac -d . src/Clock.java
	javac -d . src/WorkerQueue.java
	javac -d . src/TimeStampedMessage.java
	javac -d . src/LogicalClock.java
	javac -d . src/VectorClock.java
	javac -classpath lib/snakeyaml-1.11.jar:. -d . src/MessagePasser.java
	javac -d . src/ClockFactory.java
	javac -d . src/Sender.java
	javac -d . src/ReceiverWorker.java
	javac -d . src/Receiver.java
	javac -d . src/FileMonitor.java
	javac -classpath lib/snakeyaml-1.11.jar:. -d . src/lab1.java
	javac -d . src/logWorker.java
	javac -d . src/logListener.java
	javac -d . src/logger.java
