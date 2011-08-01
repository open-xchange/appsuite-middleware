
package com._4psa.resellerdata_xsd._2_5;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com._4psa.resellerdata_xsd._2_5 package.
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.resellerdata_xsd._2_5
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResellerList }
     *
     */
    public ResellerList createResellerList() {
        return new ResellerList();
    }

    /**
     * Create an instance of {@link ExtendedResellerInfo }
     *
     */
    public ExtendedResellerInfo createExtendedResellerInfo() {
        return new ExtendedResellerInfo();
    }

    /**
     * Create an instance of {@link ResellerInfo }
     *
     */
    public ResellerInfo createResellerInfo() {
        return new ResellerInfo();
    }

    /**
     * Create an instance of {@link ResellerPLInfo }
     *
     */
    public ResellerPLInfo createResellerPLInfo() {
        return new ResellerPLInfo();
    }

    /**
     * Create an instance of {@link UpdateResellerPLInfo }
     *
     */
    public UpdateResellerPLInfo createUpdateResellerPLInfo() {
        return new UpdateResellerPLInfo();
    }

}
