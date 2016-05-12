package naming;

import common.Path;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class DfsLock {
    public Path lockedPath;
    public boolean isExclusive;
    public CountDownLatch notification;

    public DfsLock(Path path, boolean exclusive) {
        lockedPath = path;
        isExclusive = exclusive;
        notification = new CountDownLatch(1);
    }

    public void notifySender(){
        notification.countDown();
    }

    public void waitLock() throws InterruptedException {
        notification.await();
    }
}
