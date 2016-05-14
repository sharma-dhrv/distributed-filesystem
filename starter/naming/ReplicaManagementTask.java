package naming;

import common.*;

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

	}

}