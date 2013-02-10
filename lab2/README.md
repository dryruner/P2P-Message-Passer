# Progress
* design - done, design has changed a little after reading through handout again
* implementation - in process:
	* parsing .yaml file - done
	* Message class - done
	* Refined again command line User Interface(lab2.java) - done
	* receive() - done
	* send() - done
	* socket programming - done
	* thread issue - done
	* reuse sockets(OutputStream) - done
	* concurrency control - done
	* adapted to modification of config.yaml - done.
	* LogicalClock/VectorClock - done
	* TimeStampedMessage - done
	* logger, choose whether or not to log this event - done
	* bug fixed - NullPointer bug fixed, if user tries to send msg to a non-exist user
	* group and mark concurrent event/msg - done
	* reliable multicast - one could choose which group he wants to multicast, and reliable multicast is ensured by replying NACK msgs when something is missing.
<br/>
* How to use it?
<br/>In the lab2 directory,
	* $ make
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.lab2 \<conf_filename\> \<local_name\>
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.logger \<conf_filename\> logger
	* NOTE: 1. logger is an independent program that the entire distributed system should only start it once.
