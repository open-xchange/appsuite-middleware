
package com.openexchange.admin.soap.util.dataobjects;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.openexchange.admin.soap.util.dataobjects package.
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.util.dataobjects
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MaintenanceReason }
     *
     */
    public MaintenanceReason createMaintenanceReason() {
        return new MaintenanceReason();
    }

    /**
     * Create an instance of {@link Credentials }
     *
     */
    public Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Create an instance of {@link Filestore }
     *
     */
    public Filestore createFilestore() {
        return new Filestore();
    }

    /**
     * Create an instance of {@link Database }
     *
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link Server }
     *
     */
    public Server createServer() {
        return new Server();
    }

}
