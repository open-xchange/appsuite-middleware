
package com._4psa.resellermessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;
import com._4psa.resellerdata_xsd._2_5.ResellerPLInfo;
import com._4psa.resellerdata_xsd._2_5.UpdateResellerPLInfo;
import com._4psa.resellermessagesinfo_xsd._2_5.GetResellerDetailsResponseType;
import com._4psa.resellermessagesinfo_xsd._2_5.GetResellerPLResponseType;
import com._4psa.resellermessagesinfo_xsd._2_5.GetResellersResponseType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.resellermessages_xsd._2_5 package. 
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

    private final static QName _UpdateResellerPLResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "UpdateResellerPLResponse");
    private final static QName _SetResellerPLRequest_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "SetResellerPLRequest");
    private final static QName _DelResellerResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "DelResellerResponse");
    private final static QName _UpdateResellerPLRequest_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "UpdateResellerPLRequest");
    private final static QName _SetResellerStatusResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "SetResellerStatusResponse");
    private final static QName _SetResellerPLResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "SetResellerPLResponse");
    private final static QName _GetResellersResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "GetResellersResponse");
    private final static QName _GetResellerDetailsRequest_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "GetResellerDetailsRequest");
    private final static QName _GetResellerDetailsResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "GetResellerDetailsResponse");
    private final static QName _SetResellerCpAccessResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "SetResellerCpAccessResponse");
    private final static QName _GetResellerPLResponse_QNAME = new QName("http://4psa.com/ResellerMessages.xsd/2.5.1", "GetResellerPLResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.resellermessages_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetResellerPLRequest }
     * 
     */
    public GetResellerPLRequest createGetResellerPLRequest() {
        return new GetResellerPLRequest();
    }

    /**
     * Create an instance of {@link EditResellerResponse }
     * 
     */
    public EditResellerResponse createEditResellerResponse() {
        return new EditResellerResponse();
    }

    /**
     * Create an instance of {@link EditResellerRequest }
     * 
     */
    public EditResellerRequest createEditResellerRequest() {
        return new EditResellerRequest();
    }

    /**
     * Create an instance of {@link SetResellerStatusRequest }
     * 
     */
    public SetResellerStatusRequest createSetResellerStatusRequest() {
        return new SetResellerStatusRequest();
    }

    /**
     * Create an instance of {@link AddResellerResponse }
     * 
     */
    public AddResellerResponse createAddResellerResponse() {
        return new AddResellerResponse();
    }

    /**
     * Create an instance of {@link GetResellersRequest }
     * 
     */
    public GetResellersRequest createGetResellersRequest() {
        return new GetResellersRequest();
    }

    /**
     * Create an instance of {@link SetResellerCpAccessRequest }
     * 
     */
    public SetResellerCpAccessRequest createSetResellerCpAccessRequest() {
        return new SetResellerCpAccessRequest();
    }

    /**
     * Create an instance of {@link GetResellerDetailsRequest }
     * 
     */
    public GetResellerDetailsRequest createGetResellerDetailsRequest() {
        return new GetResellerDetailsRequest();
    }

    /**
     * Create an instance of {@link DelResellerRequest }
     * 
     */
    public DelResellerRequest createDelResellerRequest() {
        return new DelResellerRequest();
    }

    /**
     * Create an instance of {@link AddResellerRequest }
     * 
     */
    public AddResellerRequest createAddResellerRequest() {
        return new AddResellerRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "UpdateResellerPLResponse")
    public JAXBElement<UpdateObject> createUpdateResellerPLResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_UpdateResellerPLResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResellerPLInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "SetResellerPLRequest")
    public JAXBElement<ResellerPLInfo> createSetResellerPLRequest(ResellerPLInfo value) {
        return new JAXBElement<ResellerPLInfo>(_SetResellerPLRequest_QNAME, ResellerPLInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "DelResellerResponse")
    public JAXBElement<DelObject> createDelResellerResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelResellerResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateResellerPLInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "UpdateResellerPLRequest")
    public JAXBElement<UpdateResellerPLInfo> createUpdateResellerPLRequest(UpdateResellerPLInfo value) {
        return new JAXBElement<UpdateResellerPLInfo>(_UpdateResellerPLRequest_QNAME, UpdateResellerPLInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "SetResellerStatusResponse")
    public JAXBElement<UpdateObject> createSetResellerStatusResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetResellerStatusResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "SetResellerPLResponse")
    public JAXBElement<UpdateObject> createSetResellerPLResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetResellerPLResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResellersResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "GetResellersResponse")
    public JAXBElement<GetResellersResponseType> createGetResellersResponse(GetResellersResponseType value) {
        return new JAXBElement<GetResellersResponseType>(_GetResellersResponse_QNAME, GetResellersResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResellerDetailsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "GetResellerDetailsRequest")
    public JAXBElement<GetResellerDetailsRequest> createGetResellerDetailsRequest(GetResellerDetailsRequest value) {
        return new JAXBElement<GetResellerDetailsRequest>(_GetResellerDetailsRequest_QNAME, GetResellerDetailsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResellerDetailsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "GetResellerDetailsResponse")
    public JAXBElement<GetResellerDetailsResponseType> createGetResellerDetailsResponse(GetResellerDetailsResponseType value) {
        return new JAXBElement<GetResellerDetailsResponseType>(_GetResellerDetailsResponse_QNAME, GetResellerDetailsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "SetResellerCpAccessResponse")
    public JAXBElement<UpdateObject> createSetResellerCpAccessResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_SetResellerCpAccessResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResellerPLResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ResellerMessages.xsd/2.5.1", name = "GetResellerPLResponse")
    public JAXBElement<GetResellerPLResponseType> createGetResellerPLResponse(GetResellerPLResponseType value) {
        return new JAXBElement<GetResellerPLResponseType>(_GetResellerPLResponse_QNAME, GetResellerPLResponseType.class, null, value);
    }

}
