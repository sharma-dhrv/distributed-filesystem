package naming;

import common.Path;
import storage.Command;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class StorageInfo {
    public Command stub;
    public ArrayList<TreeNode> paths;

    public StorageInfo(Command command){
        stub = command;
        paths = new ArrayList<TreeNode>();
    }

    public void addPaths(List<Path> paths){
        throw new NotImplementedException();
    }

    public void addPath(Path path){
        throw new NotImplementedException();
    }

    public void removePath(Path path){
        throw new NotImplementedException();
    }

    public void deleteStub(){
        throw new NotImplementedException();
    }
}
