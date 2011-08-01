
package com._4psa.accountmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com._4psa.accountmessages_xsd._2_5 package.
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

    private final static QName _DelAccountResponse_QNAME = new QName("http://4psa.com/AccountMessages.xsd/2.5.1", "DelAccountResponse");
    private final static QName _SetAccountStatusResponse_QNAME = new QName("http://4psa.com/AccountMessages.xsd/2.5.1", "SetAccountStatusResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.accountmessages_xsd._2_5
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LinkAccountResponse }
     *
     */
    public LinkAccountResponse createLinkAccountResponse() {
        return new LinkAccountResponse();
    }

    /**
     * Create an instance of {@link SetAccountStatusRequest }
     *
     */
    public SetAccountStatusRequest createSetAccountStatusRequest() {
        return new SetAccountStatusRequest();
    }

    /**
     * Create an instance of {@link LinkAccountRequest }
     *
     */
    public LinkAccountRequest createLinkAccountRequest() {
        return new LinkAccountRequest();
    }

    /**
     * Create an instance of {@link DelAccountRequest }
     *
     */
    public DelAccountRequest createDelAccountRequest() {
        return new DelAccountRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/AccountMessages.xsd/2.5.1", name = "DelAccountResponse")
    public JAXBElement<DelObject> createDelAccountResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelAccountResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/AccountMessages.xsd/2.5.1", name = "SetAccountStatusResponse")
    public JAXBElement<UpdateObject> createSetAccountStatusResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetAccountStatusResponse_QNAME, UpdateObject.class, null, value);
    }

}
