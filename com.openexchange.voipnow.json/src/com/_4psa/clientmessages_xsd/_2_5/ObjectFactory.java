
package com._4psa.clientmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.clientmessagesinfo_xsd._2_5.GetClientPLResponseType;
import com._4psa.clientmessagesinfo_xsd._2_5.GetClientResponseType;
import com._4psa.clientmessagesinfo_xsd._2_5.MoveClientsResponseType;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com._4psa.clientmessages_xsd._2_5 package.
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

    private final static QName _UpdateClientPLResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "UpdateClientPLResponse");
    private final static QName _DelClientResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "DelClientResponse");
    private final static QName _MoveClientsResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "MoveClientsResponse");
    private final static QName _GetClientPLResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "GetClientPLResponse");
    private final static QName _SetClientPLResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "SetClientPLResponse");
    private final static QName _SetClientCpAccessResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "SetClientCpAccessResponse");
    private final static QName _SetClientStatusResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "SetClientStatusResponse");
    private final static QName _GetClientsResponse_QNAME = new QName("http://4psa.com/ClientMessages.xsd/2.5.1", "GetClientsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.clientmessages_xsd._2_5
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DelClientRequest }
     *
     */
    public DelClientRequest createDelClientRequest() {
        return new DelClientRequest();
    }

    /**
     * Create an instance of {@link MoveClientsRequest }
     *
     */
    public MoveClientsRequest createMoveClientsRequest() {
        return new MoveClientsRequest();
    }

    /**
     * Create an instance of {@link AddClientRequest }
     *
     */
    public AddClientRequest createAddClientRequest() {
        return new AddClientRequest();
    }

    /**
     * Create an instance of {@link GetClientDetailsRequest }
     *
     */
    public GetClientDetailsRequest createGetClientDetailsRequest() {
        return new GetClientDetailsRequest();
    }

    /**
     * Create an instance of {@link SetClientCpAccessRequest }
     *
     */
    public SetClientCpAccessRequest createSetClientCpAccessRequest() {
        return new SetClientCpAccessRequest();
    }

    /**
     * Create an instance of {@link GetClientDetailsResponse }
     *
     */
    public GetClientDetailsResponse createGetClientDetailsResponse() {
        return new GetClientDetailsResponse();
    }

    /**
     * Create an instance of {@link GetClientsRequest }
     *
     */
    public GetClientsRequest createGetClientsRequest() {
        return new GetClientsRequest();
    }

    /**
     * Create an instance of {@link EditClientResponse }
     *
     */
    public EditClientResponse createEditClientResponse() {
        return new EditClientResponse();
    }

    /**
     * Create an instance of {@link EditClientRequest }
     *
     */
    public EditClientRequest createEditClientRequest() {
        return new EditClientRequest();
    }

    /**
     * Create an instance of {@link AddClientResponse }
     *
     */
    public AddClientResponse createAddClientResponse() {
        return new AddClientResponse();
    }

    /**
     * Create an instance of {@link UpdateClientPLRequest }
     *
     */
    public UpdateClientPLRequest createUpdateClientPLRequest() {
        return new UpdateClientPLRequest();
    }

    /**
     * Create an instance of {@link SetClientPLRequest }
     *
     */
    public SetClientPLRequest createSetClientPLRequest() {
        return new SetClientPLRequest();
    }

    /**
     * Create an instance of {@link GetClientPLRequest }
     *
     */
    public GetClientPLRequest createGetClientPLRequest() {
        return new GetClientPLRequest();
    }

    /**
     * Create an instance of {@link SetClientStatusRequest }
     *
     */
    public SetClientStatusRequest createSetClientStatusRequest() {
        return new SetClientStatusRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "UpdateClientPLResponse")
    public JAXBElement<UpdateObject> createUpdateClientPLResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_UpdateClientPLResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "DelClientResponse")
    public JAXBElement<DelObject> createDelClientResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelClientResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MoveClientsResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "MoveClientsResponse")
    public JAXBElement<MoveClientsResponseType> createMoveClientsResponse(MoveClientsResponseType value) {
        return new JAXBElement<MoveClientsResponseType>(_MoveClientsResponse_QNAME, MoveClientsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetClientPLResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "GetClientPLResponse")
    public JAXBElement<GetClientPLResponseType> createGetClientPLResponse(GetClientPLResponseType value) {
        return new JAXBElement<GetClientPLResponseType>(_GetClientPLResponse_QNAME, GetClientPLResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "SetClientPLResponse")
    public JAXBElement<UpdateObject> createSetClientPLResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetClientPLResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "SetClientCpAccessResponse")
    public JAXBElement<UpdateObject> createSetClientCpAccessResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetClientCpAccessResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "SetClientStatusResponse")
    public JAXBElement<UpdateObject> createSetClientStatusResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetClientStatusResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetClientResponseType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://4psa.com/ClientMessages.xsd/2.5.1", name = "GetClientsResponse")
    public JAXBElement<GetClientResponseType> createGetClientsResponse(GetClientResponseType value) {
        return new JAXBElement<GetClientResponseType>(_GetClientsResponse_QNAME, GetClientResponseType.class, null, value);
    }

}
