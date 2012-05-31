
package com.openexchange.admin.soap.taskmgmt;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.admin.soap.taskmgmt package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.taskmgmt
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link InvalidDataException }
     * 
     */
    public InvalidDataException createInvalidDataException() {
        return new InvalidDataException();
    }

    /**
     * Create an instance of {@link GetJobListResponse }
     * 
     */
    public GetJobListResponse createGetJobListResponse() {
        return new GetJobListResponse();
    }

    /**
     * Create an instance of {@link TaskManagerException }
     * 
     */
    public TaskManagerException createTaskManagerException() {
        return new TaskManagerException();
    }

    /**
     * Create an instance of {@link StorageException }
     * 
     */
    public StorageException createStorageException() {
        return new StorageException();
    }

    /**
     * Create an instance of {@link Flush }
     * 
     */
    public Flush createFlush() {
        return new Flush();
    }

    /**
     * Create an instance of {@link InvalidCredentialsException }
     * 
     */
    public InvalidCredentialsException createInvalidCredentialsException() {
        return new InvalidCredentialsException();
    }

    /**
     * Create an instance of {@link DeleteJob }
     * 
     */
    public DeleteJob createDeleteJob() {
        return new DeleteJob();
    }

    /**
     * Create an instance of {@link ExecutionException }
     * 
     */
    public ExecutionException createExecutionException() {
        return new ExecutionException();
    }

    /**
     * Create an instance of {@link GetTaskResults }
     * 
     */
    public GetTaskResults createGetTaskResults() {
        return new GetTaskResults();
    }

    /**
     * Create an instance of {@link GetTaskResultsResponse }
     * 
     */
    public GetTaskResultsResponse createGetTaskResultsResponse() {
        return new GetTaskResultsResponse();
    }

    /**
     * Create an instance of {@link GetJobList }
     * 
     */
    public GetJobList createGetJobList() {
        return new GetJobList();
    }

    /**
     * Create an instance of {@link InterruptedException }
     * 
     */
    public InterruptedException createInterruptedException() {
        return new InterruptedException();
    }

    /**
     * Create an instance of {@link RemoteException }
     * 
     */
    public RemoteException createRemoteException() {
        return new RemoteException();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

}
