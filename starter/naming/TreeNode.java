package naming;

import common.Path;

import java.util.ArrayList;
import java.util.HashMap;
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
    public ArrayList<String> storages;

    // Locks
    public ConcurrentLinkedQueue<DfsLock> currentLocks = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<DfsLock> pendingLocks = new ConcurrentLinkedQueue<>();


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
}
