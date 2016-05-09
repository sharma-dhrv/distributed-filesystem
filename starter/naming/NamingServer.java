package naming;

import java.io.*;
import java.net.*;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
    TreeNode filesystem = new TreeNode();
    private HashMap<String, StorageInfo> availableStorage;
    private Skeleton<Registration> registrationSkeleton;
    private Skeleton<Service> serviceSkeleton;
    private boolean wasStartAttempted = false;

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        if (wasStartAttempted){
            throw new RMIException("Attempt to restart failed naming server");
        }
        try{
            InetSocketAddress regAddress = new InetSocketAddress(NamingStubs.REGISTRATION_PORT);
            registrationSkeleton = new Skeleton<>(Registration.class, new RegistrationImpl(), regAddress);
            registrationSkeleton.start();

            InetSocketAddress serviceAddress = new InetSocketAddress(NamingStubs.SERVICE_PORT);
            serviceSkeleton = new Skeleton<>(Service.class, new ServiceImpl(), serviceAddress);
            serviceSkeleton.start();
        } finally {
            wasStartAttempted = true;
        }
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
        if (wasStartAttempted){
            registrationSkeleton.stop();
            serviceSkeleton.stop();
            // TODO: interrupt as many of the threads that are executing naming server code as possible
        }
        stopped(null);
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    protected TreeNode getNode(Path path){
        TreeNode current = filesystem;

        if (path.isRoot()){
            return filesystem;
        }

        for (String component: path)
        {
            if (current.hasChild(component)){
                current = current.getChild(component);
            } else {
                return null;
            }
        }
        return current;
    }

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        TreeNode node = getNode(path);
        return node.nodeType == TreeNode.NodeType.DIRECTORY;
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        TreeNode node = getNode(directory);
        return (String[]) node.children.keySet().toArray();
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if (directory == null){
            throw new NullPointerException("Given null directory arg");
        }
        if (directory.equals(new Path("/"))){
            return false;
        }
        Path parent = new Path(directory);
        parent.removeLastComponent();
        TreeNode node = getNode(parent);
        if (node == null){
            throw new FileNotFoundException("Directory not found " + parent.toString());
        }
        if (node.nodeType != TreeNode.NodeType.DIRECTORY){
            throw new FileNotFoundException("Attempt to create directory inside of the file " + parent.toString());
        }
        if (node.hasChild(directory.last())){
            return false;
        }
        return createDirectoryInTree(node, directory);
    }

    private boolean createDirectoryInTree(TreeNode parent, Path directory){
        TreeNode newDir = new TreeNode(parent, directory.last(), TreeNode.NodeType.DIRECTORY);
        parent.children.put(directory.last(), newDir);
        return true;
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        throw new UnsupportedOperationException("not implemented");
    }
}
