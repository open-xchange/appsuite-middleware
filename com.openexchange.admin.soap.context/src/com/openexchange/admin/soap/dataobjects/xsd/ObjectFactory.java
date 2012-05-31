
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.admin.soap.dataobjects.xsd package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.dataobjects.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Context }
     * 
     */
    public Context createContext() {
        return new Context();
    }

    /**
     * Create an instance of {@link Database }
     * 
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link Filestore }
     * 
     */
    public Filestore createFilestore() {
        return new Filestore();
    }

    /**
     * Create an instance of {@link User }
     * 
     */
    public User createUser() {
        return new User();
    }

    /**
     * Create an instance of {@link UserModuleAccess }
     * 
     */
    public UserModuleAccess createUserModuleAccess() {
        return new UserModuleAccess();
    }

    /**
     * Create an instance of {@link Entry }
     * 
     */
    public Entry createEntry() {
        return new Entry();
    }

    /**
     * Create an instance of {@link SOAPStringMap }
     * 
     */
    public SOAPStringMap createSOAPStringMap() {
        return new SOAPStringMap();
    }

    /**
     * Create an instance of {@link SOAPStringMapMap }
     * 
     */
    public SOAPStringMapMap createSOAPStringMapMap() {
        return new SOAPStringMapMap();
    }

    /**
     * Create an instance of {@link SOAPMapEntry }
     * 
     */
    public SOAPMapEntry createSOAPMapEntry() {
        return new SOAPMapEntry();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

}
