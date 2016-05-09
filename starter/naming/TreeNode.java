package naming;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class TreeNode {
    public TreeNode parent;
    public ArrayList<TreeNode> children;
    public int numAccesses;
    public ConcurrentLinkedQueue<DfsLock> current;
    public ConcurrentLinkedQueue<DfsLock> pending;
    public ArrayList<String> storages;

    public TreeNode(){}

    public TreeNode(TreeNode parent){
        this.parent = parent;
    }

}
