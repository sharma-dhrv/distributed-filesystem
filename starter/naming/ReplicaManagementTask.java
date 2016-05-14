package naming;

import common.*;
import java.util.*;
import storage.*;

/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

public class ReplicaManagementTask implements Runnable {

	private Path file;
	private TreeNode node;
	private NamingServer namingServer;

	/**
	 * The current storage node on which read or write operation happens.
	 */
	private StorageInfo currentStorageInfo;

	/**
	 * It is True if this task must do a file replication to new storage nodes;
	 * False if it is an invalidation task to delete nodes from all other
	 * storage nodes.
	 */
	private boolean isReplicationTask;

	public ReplicaManagementTask(NamingServer namingServer, Path file, TreeNode node, StorageInfo currentStorageInfo,
			boolean isReplicationTask) {
		this.file = file;
		this.node = node;
		this.namingServer = namingServer;
		this.currentStorageInfo = currentStorageInfo;
		this.isReplicationTask = isReplicationTask;
	}

	public void run() {

		// TODO: perform replication or invalidation based on isReplicationTask;
		// (try to) acquire locks accordingly for replication and invalidation
		// on other servers
		
		// Do file replication on new storage nodes
		if (isReplicationTask) {
			Storage src = currentStorageInfo.clientStub;
			Storage dest = null;

			HashSet<StorageInfo> availableStorages = namingServer.availableStorages;
			Iterator<StorageInfo> it = availableStorages.iterator();
			while(it.hasNext()) {
				StorageInfo considerInfo = it.next();
				Storage consider = considerInfo.clientStub;
				if (consider != src) {
					dest = consider;
					break;
				}
			}
			if (dest == null) {
				System.out.println("[ERROR] No available storage servers to replicate file. Need Patience!");
			}
	
			
			Command cmd_stub = dest.clientStub;
			try {
				cmd_stub.copy(file, src);
			} catch(Exception e) {
				System.out.println("[ERROR] Replication on new storage nodes failed!");
				e.printStackTrace();
			}
		// Do invalidate operation on all other storage nodes
		} else {
			Storage src = currentStorageInfo.clientStub;

			Storage dest = null;

                        HashSet<StorageInfo> availableStorages = namingServer.availableStorages;
                        Iterator<StorageInfo> it = availableStorages.iterator();
                        while(it.hasNext()) {
                                StorageInfo considerInfo = it.next();
                                Storage consider = considerInfo.clientStub;
				try {
					if (consider != src) {
						consider.delete(file);
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Failed during invalidation of replicas");
					e.printStackTrace();
				}
                        }

		}
	}

}
