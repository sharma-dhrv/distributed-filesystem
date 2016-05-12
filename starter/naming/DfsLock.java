package naming;

import common.Path;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class DfsLock {
    public Path lockedPath;
    public enum LockType {SHARED, EXCLUSIVE};
    public LockType type;
}
