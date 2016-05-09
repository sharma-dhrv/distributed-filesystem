package naming;

import common.Path;
import rmi.RMIException;
import storage.Storage;

import java.io.FileNotFoundException;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class ServiceImpl implements Service {
    @Override
    public void lock(Path path, boolean exclusive) throws RMIException, FileNotFoundException {

    }

    @Override
    public void unlock(Path path, boolean exclusive) throws RMIException {

    }

    @Override
    public boolean isDirectory(Path path) throws RMIException, FileNotFoundException {
        return false;
    }

    @Override
    public String[] list(Path directory) throws RMIException, FileNotFoundException {
        return new String[0];
    }

    @Override
    public boolean createFile(Path file) throws RMIException, FileNotFoundException {
        return false;
    }

    @Override
    public boolean createDirectory(Path directory) throws RMIException, FileNotFoundException {
        return false;
    }

    @Override
    public boolean delete(Path path) throws RMIException, FileNotFoundException {
        return false;
    }

    @Override
    public Storage getStorage(Path file) throws RMIException, FileNotFoundException {
        return null;
    }
}
