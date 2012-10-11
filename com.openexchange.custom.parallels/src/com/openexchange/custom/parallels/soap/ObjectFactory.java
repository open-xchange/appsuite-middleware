
package com.openexchange.custom.parallels.soap;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.custom.parallels.soap package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.custom.parallels.soap
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
     * Create an instance of {@link StorageException }
     * 
     */
    public StorageException createStorageException() {
        return new StorageException();
    }

    /**
     * Create an instance of {@link InvalidCredentialsException }
     * 
     */
    public InvalidCredentialsException createInvalidCredentialsException() {
        return new InvalidCredentialsException();
    }

    /**
     * Create an instance of {@link GetServerBundleList }
     * 
     */
    public GetServerBundleList createGetServerBundleList() {
        return new GetServerBundleList();
    }

    /**
     * Create an instance of {@link GetServerVersion }
     * 
     */
    public GetServerVersion createGetServerVersion() {
        return new GetServerVersion();
    }

    /**
     * Create an instance of {@link GetServerBundleListResponse }
     * 
     */
    public GetServerBundleListResponse createGetServerBundleListResponse() {
        return new GetServerBundleListResponse();
    }

    /**
     * Create an instance of {@link Bundle }
     * 
     */
    public Bundle createBundle() {
        return new Bundle();
    }

    /**
     * Create an instance of {@link GetServerVersionResponse }
     * 
     */
    public GetServerVersionResponse createGetServerVersionResponse() {
        return new GetServerVersionResponse();
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
