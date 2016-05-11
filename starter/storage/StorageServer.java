package storage;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
    public File root;

    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root, int client_port, int command_port)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        throw new UnsupportedOperationException("not implemented");
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        File f = file.toFile(root);

        if (f != null && f.exists() && f.isFile()) {
          return f.length();
        } else {
          throw new FileNotFoundException("Size cannot be obtained for File" + f);
        }
    }

    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        File fileRead = file.toFile(root);
        FileInputStream fis;
        byte[] buffer;

        if (fileRead != null && !fileRead.exists()) {
            if (!fileRead.isFile()) {
                if (offset >= 0 && offset + length <= size(file) && length >= 0) {
                  fis = new FileInputStream(fileRead);
                  fis.skip(offset);
                  buffer = new byte[length];
                  try {
                    fis.read(buffer);
                  } catch(IOException e) {
                    fis.close();
                    System.out.println(e);
                  }
                } else {
                  throw new IndexOutOfBoundsException("Length and offset should be positive");
                }
            } else {
              throw new FileNotFoundException("Not a file");
            }
        } else {
          throw new FileNotFoundException("File doesn't exist");
        }
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
      File fileToWrite = file.toFile (root);
      FileChannel channel;
      long bytesWritten;

      if (fileToWrite.isFile()) {
          if (offset >= 0) {
            channel = new FileOutputStream(fileToWrite).getChannel();
            channel.position(offset);
            bytesWritten = channel.write(ByteBuffer.wrap (data));

            if (data.length != bytesWritten) {
                throw new IOException ("Failed in writing data to the file. Wrote " +
                        bytesWritten + " instead of " + data.length);
            }
          } else {
            throw new IndexOutOfBoundsException("Offset cannot be negative.");
          }
        } else {
            throw new FileNotFoundException(fileToWrite + " is not a file.");
        }
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
      File fileToCreate = file.toFile(root);
      if (file.isRoot()) {
        return false;
      }

      File parentFile = file.parent().toFile(root);

      if (!parentFile.isDirectory()) {
        parentFile.mkdirs();
      }

      try {
        return fileToCreate.createNewFile();
      } catch (IOException e) {
        return false;
      }
    }

    @Override
    public synchronized boolean delete(Path path)
    {
      File fileToDelete = path.toFile(root);

      if (path.isRoot()) {
        return false;
      }

      if (fileToDelete.isFile()) {
        return fileToDelete.delete();
      } else {
        File[] fileList = fileToDelete.listFiles();

        if (fileList != null) {
          for (File fil : fileList) {
            if(deleteDir(fil) == false){
              return false;
            }
            }
        }
      return fileToDelete.delete();
      }
    }

    private boolean deleteDir(File f) {
    	File[] fileList = f.listFiles();

    	if (fileList != null) {
    		for (File f1 : fileList) {
    			if(deleteDir(f1) == false){
    				return false;
    			}
        	}
    	}

		  return f.delete();
    }

    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
      File f = file.toFile(root);
      long fSize = server.size(file);
      byte[] bytes;
      int reads = Integer.MAX_VALUE;

      if (f.exists()) {
        f.delete();
      }
      create(file);

      for(long offset = 0; offset < fSize; offset += reads) {
        reads = (int) Math.min(reads, fSize - offset);
        bytes = server.read(file, offset, reads);
        write(file, offset, bytes);
      }

      return true;
    }
}
