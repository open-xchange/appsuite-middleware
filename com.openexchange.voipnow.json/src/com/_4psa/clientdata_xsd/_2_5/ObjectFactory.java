
package com._4psa.clientdata_xsd._2_5;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.clientdata_xsd._2_5 package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.clientdata_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExtendedClientInfo }
     * 
     */
    public ExtendedClientInfo createExtendedClientInfo() {
        return new ExtendedClientInfo();
    }

    /**
     * Create an instance of {@link ClientInfo }
     * 
     */
    public ClientInfo createClientInfo() {
        return new ClientInfo();
    }

    /**
     * Create an instance of {@link ExtendedClientInfo.Link }
     * 
     */
    public ExtendedClientInfo.Link createExtendedClientInfoLink() {
        return new ExtendedClientInfo.Link();
    }

    /**
     * Create an instance of {@link UpdateClientPLInfo }
     * 
     */
    public UpdateClientPLInfo createUpdateClientPLInfo() {
        return new UpdateClientPLInfo();
    }

    /**
     * Create an instance of {@link ClientPLInfo }
     * 
     */
    public ClientPLInfo createClientPLInfo() {
        return new ClientPLInfo();
    }

    /**
     * Create an instance of {@link ClientList }
     * 
     */
    public ClientList createClientList() {
        return new ClientList();
    }

}
