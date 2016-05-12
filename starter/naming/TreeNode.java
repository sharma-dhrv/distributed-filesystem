package naming;

import common.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class TreeNode {

    public enum NodeType {FILE, DIRECTORY};

    // Structure
    public NodeType nodeType;
    public String nodeName;
    public TreeNode parent;
    public HashMap<String, TreeNode> children = new HashMap<>();

    // Metadata
    public int numAccesses;
    public ArrayList<StorageInfo> storages = new ArrayList<>();

    // Locks
    public LinkedList<DfsLock> currentLocks = new LinkedList<>();
    public LinkedList<DfsLock> pendingLocks = new LinkedList<>();


    public TreeNode(){}

    public TreeNode(TreeNode parent){
        this.parent = parent;
    }

    public TreeNode(TreeNode parent, String current, NodeType type) {
        this.parent = parent;
        nodeName = current;
        nodeType = type;
    }

    public boolean hasChild(String component){
        // TODO: check if contains will compare by value, not by reference
        return children.containsKey(component);
    }
    public TreeNode getChild(String component){
        return children.get(component);
    }

    public void addStorage(StorageInfo storage){
        storages.add(storage);
    }

    public TreeNode addChild(TreeNode child) {
        child.parent = this;
        children.put(child.nodeName, child);
        return child;
    }

    public void addLock(DfsLock dfsLock) {
        pendingLocks.add(dfsLock);
        checkPendingQueue();
    }

    public void removeLock(Path path) {
        for (DfsLock dfsLock: currentLocks){
            if (dfsLock.lockedPath.equals(path)){
                currentLocks.remove(dfsLock);
                checkPendingQueue();
                return;
            }
        }
        // TODO: do we need to check locks in pendingLocks? Maybe released lock hasn't been even aquired
    }

    private void checkPendingQueue() {
        DfsLock dfsLock = pendingLocks.peek();
        if (dfsLock != null){
            boolean canMoveRW = currentLocks.isEmpty();
            boolean canMoveR = currentLocks.isEmpty() && !currentLocks.peek().isExclusive && !dfsLock.isExclusive;
            if (canMoveRW || canMoveR){
                pendingLocks.pollFirst();
                currentLocks.add(dfsLock);
                checkNotifySender(dfsLock);
            }
        }
    }

    private void checkNotifySender(DfsLock dfsLock) {
        Path pathToCurrent = getPathToCurrent();
        if (dfsLock.lockedPath.equals(pathToCurrent)){
            dfsLock.notifySender();
        }
    }

    private Path getPathToCurrent() {

        StringBuilder path = new StringBuilder();
        TreeNode current = this;
        if (current.parent == null){
            return new Path();
        }
        while (current.parent != null){
            path.insert(0, "/"+nodeName);
            current = current.parent;
        }
        return new Path(path.toString());
    }

}
