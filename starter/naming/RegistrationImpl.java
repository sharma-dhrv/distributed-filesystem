package naming;

import common.Path;
import rmi.RMIException;
import storage.Command;
import storage.Storage;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class RegistrationImpl implements Registration {
    @Override
    public Path[] register(Storage client_stub, Command command_stub, Path[] files) throws RMIException {
        return new Path[0];
    }
}
