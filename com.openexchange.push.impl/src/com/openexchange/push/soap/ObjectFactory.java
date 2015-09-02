
package com.openexchange.push.soap;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.push.soap package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.push.soap
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ListRegisteredPushUsers }
     * 
     */
    public ListRegisteredPushUsers createListRegisteredPushUsers() {
        return new ListRegisteredPushUsers();
    }

    /**
     * Create an instance of {@link ListPushUsersResponse }
     * 
     */
    public ListPushUsersResponse createListPushUsersResponse() {
        return new ListPushUsersResponse();
    }

    /**
     * Create an instance of {@link PushUserInfo }
     * 
     */
    public PushUserInfo createPushUserInfo() {
        return new PushUserInfo();
    }

    /**
     * Create an instance of {@link ListPushUsers }
     * 
     */
    public ListPushUsers createListPushUsers() {
        return new ListPushUsers();
    }

    /**
     * Create an instance of {@link ListRegisteredPushUsersResponse }
     * 
     */
    public ListRegisteredPushUsersResponse createListRegisteredPushUsersResponse() {
        return new ListRegisteredPushUsersResponse();
    }

    /**
     * Create an instance of {@link PushUserClient }
     * 
     */
    public PushUserClient createPushUserClient() {
        return new PushUserClient();
    }

    /**
     * Create an instance of {@link UnregisterPermanentListenerFor }
     * 
     */
    public UnregisterPermanentListenerFor createUnregisterPermanentListenerFor() {
        return new UnregisterPermanentListenerFor();
    }

    /**
     * Create an instance of {@link UnregisterPermanentListenerForResponse }
     * 
     */
    public UnregisterPermanentListenerForResponse createUnregisterPermanentListenerForResponse() {
        return new UnregisterPermanentListenerForResponse();
    }

    /**
     * Create an instance of {@link PushSoapInterfaceException }
     * 
     */
    public PushSoapInterfaceException createPushSoapInterfaceException() {
        return new PushSoapInterfaceException();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link PushUser }
     * 
     */
    public PushUser createPushUser() {
        return new PushUser();
    }

}
