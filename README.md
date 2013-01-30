# Progress
* design - done, design has changed a little after reading through handout again
* implementation - in process:
	* parsing .yaml file - done
	* Message class - done
	* Command line User Interface(lab0.java) - done
	* receive() - done
	* send() - done
	* socket programming - done
	* thread issue - done
	* reuse sockets(OutputStream) - done
	* concurrency control - done
	* adapted to modification of config.yaml - done.
	* LogicalClock/VectorClock - done
	* TimeStampedMessage - done
	* logger server - done
	* bug fixed - NullPointer bug fixed, if user tries to send msg to a non-exist user
	* group and mark concurrent event/msg - done

* How to use it?
<br/>In the lab1 directory,
	* $ make
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.lab1 \<conf_filename\> \<local_name\>
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.logger \<conf_filename\> logger
	* NOTE: 1. logger is an independent program that the entire distributed system should only start it once. 2. concurrent event and total order haven't done yet.

* Potential bugs:
MessagePasser.java line 243~250, or the whole CheckRule() function.
