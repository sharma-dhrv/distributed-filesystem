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
    private HashSet<StorageInfo> availableStorages = new HashSet<>();
    private Skeleton<Registration> registrationSkeleton;
    private Skeleton<Service> serviceSkeleton;
    private boolean wasStartAttempted = false;
    private Random random = new Random();

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer() { }

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
            registrationSkeleton = new Skeleton<>(Registration.class, this, regAddress);
            registrationSkeleton.start();

            InetSocketAddress serviceAddress = new InetSocketAddress(NamingStubs.SERVICE_PORT);
            serviceSkeleton = new Skeleton<>(Service.class, this, serviceAddress);
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

    protected boolean isValidCreationPath(Path path){
        if (path == null) {
            throw new NullPointerException("Given null creation path argument");
        }
        if (path.equals(new Path("/"))){
            return false;
        }
        return true;
    }

    protected TreeNode getParentNode(Path path){
        Path parent = new Path(path);
        parent.removeLastComponent();
        TreeNode node = getNode(parent);
        return node;
    }

    protected boolean checkParentForCreation(TreeNode parent, Path path) throws FileNotFoundException {
        if (parent == null){
            throw new FileNotFoundException("Directory not found along the path " + path.toString());
        }
        if (parent.nodeType != TreeNode.NodeType.DIRECTORY){
            throw new FileNotFoundException("Attempt to create file or directory inside of the file along the path" +
                    path.toString());
        }
        if (parent.hasChild(path.last())){
            return false;
        }
        return true;
    }

    @Override
    public boolean createFile(Path file) throws RMIException, FileNotFoundException
    {
        if (isValidCreationPath(file)){
            TreeNode parent = getParentNode(file);
            if (checkParentForCreation(parent, file)){
                return createFileInStorageAndTree(parent, file);
            }
        }
        return false;
    }

    private synchronized boolean createFileInStorageAndTree(TreeNode parent, Path file) throws RMIException {
        StorageInfo storage = chooseStorage();
        boolean result = storage.commandStub.create(file);

        if (result){
            TreeNode newNode = parent.addChild(new TreeNode(parent,file.last(), TreeNode.NodeType.FILE));
            storage.addFile(newNode);
            newNode.storages.add(storage);
            return true;
        }
        return false;
    }

    private synchronized StorageInfo chooseStorage() {
        // TODO: CHECK IF StorageInfo IS PASSED AROUND AS REFERENCE AND NOT COPY
        int choice = random.nextInt(availableStorages.size());
        return (StorageInfo) availableStorages.toArray()[choice];
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if (isValidCreationPath(directory)){
            TreeNode parent = getParentNode(directory);
            if (checkParentForCreation(parent, directory)){
                parent.addChild(new TreeNode(parent, directory.last(), TreeNode.NodeType.DIRECTORY));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        if (file == null) {
            throw new NullPointerException("Given null Path argument");
        }
        TreeNode node = getNode(file);
        if (node == null){
            throw new FileNotFoundException("File not found along the path " + file.toString());
        }
        if (node.nodeType == TreeNode.NodeType.DIRECTORY){
            throw new FileNotFoundException("Expected path to file, found directory along the path " + file.toString());
        }
        // TODO: ping the Storage Server before giving it to client. Maybe it's dead and file isn't available
        return node.storages.get(0).clientStub;
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub, Path[] files)
    {
        // TODO: add exclusive lock
        checkRegisterArgs(client_stub, command_stub, files);
        return registerStorage(client_stub, command_stub, files);

    }

    private void checkRegisterArgs(Storage client_stub, Command command_stub, Path[] files){
        if (client_stub == null || command_stub == null || files == null){
            throw new NullPointerException("Some of register arguments is null");
        }
        if (isStorageRegistered(client_stub, command_stub)){
            throw new IllegalStateException("Storage is already registered");
        }
    }

    private boolean isStorageRegistered(Storage client_stub, Command command_stub) {
        for (StorageInfo storage: availableStorages){
            if (storage.clientStub.equals(client_stub) || storage.commandStub.equals(command_stub)){
                return true;
            }
        }
        return false;
    }

    private Path[] registerStorage(Storage client_stub, Command command_stub, Path[] files){
        StorageInfo storage = new StorageInfo(client_stub, command_stub);
        ArrayList<Path> duplicatePaths = new ArrayList<>();
        for (Path path: files){
            TreeNode node = getNode(path);
            if (node == null){
                duplicatePaths.add(path);
            } else {
                node = createPathInTree(path);
                node.addStorage(storage);
                storage.addFile(node);
            }
        }
        // TODO: maybe call command_stub.delete(path) for each path in duplicatePaths (to delete physical files synchronously)
        return (Path[]) duplicatePaths.toArray();
    }

    private TreeNode createPathInTree(Path path){
        // File doesn't exist according to previous checks
        TreeNode current = createDirectoriesAlong(path.getPathWithoutLastComponent());
        return current.addChild(new TreeNode(current, path.last(), TreeNode.NodeType.FILE));
    }

    private TreeNode createDirectoriesAlong(Path path){
        TreeNode current = filesystem;

        for (String component: path)
        {
            if (!current.hasChild(component)){
                current.addChild(new TreeNode(current, component, TreeNode.NodeType.DIRECTORY));
            }
            current = current.getChild(component);
        }
        return current;
    }
}
