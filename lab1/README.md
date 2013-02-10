# Progress
* design - done, design has changed a little after reading through handout again
* implementation - in process:
	* parsing .yaml file - done
	* Message class - done
	* Refined command line User Interface(lab1.java) - done
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
<br/>
* How to use it?
<br/>In the lab1 directory,
	* $ make
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.lab1 \<conf_filename\> \<local_name\>
	* $ java -classpath lib/snakeyaml-1.11.jar:. bin.logger \<conf_filename\> logger
	* NOTE: 1. logger is an independent program that the entire distributed system should only start it once. 2. concurrent event and total order haven't done yet.
