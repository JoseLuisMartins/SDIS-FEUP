

To compile, in linux, use the follow commands:
* cd scripts/linux/
* sh compile.sh  


To run the peer, use the follow commands:
* cd scripts/linux
* sh launchServer.sh X Y 
	..* In order to be easier to launch the peers, the access point are numbers		
	..* X represents the access point of the first peer
	..* Y represents the access point of the last peer
			

To run the client:
* cd scripts/linux
* sh client.sh <peer_app> <sub-protocol> <opnd_1> <opnd_2>

	Argument description:
	- peer_app – local peer access point
	- sub-protocol – can be BACKUP, RESTORE, RECLAIM, DELETE, BACKUPENH, RESTOREENH, RECLAIMENH, DELETEENH	
	- opnd_1 – path name of the file or amount of space to be reclaim
	- opnd_2 – specifies the desired replication degree. Applies only to backup sub-protocol
