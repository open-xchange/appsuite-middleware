
package com._4psa.billingmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.billingdata_xsd._2_5.ChargingPackageList;
import com._4psa.billingdata_xsd._2_5.DestinationExceptionList;
import com._4psa.billingmessagesinfo_xsd._2_5.GetChargingPlanDetailsResponseType;
import com._4psa.billingmessagesinfo_xsd._2_5.GetChargingPlanResponseType;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.billingmessages_xsd._2_5 package. 
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

    private final static QName _EditDestinationExceptionResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "EditDestinationExceptionResponse");
    private final static QName _GetChargingPlansResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "GetChargingPlansResponse");
    private final static QName _GetChargingPackagesResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "GetChargingPackagesResponse");
    private final static QName _RechargeResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "RechargeResponse");
    private final static QName _AddChargingPackageResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "AddChargingPackageResponse");
    private final static QName _DelChargingPackageResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "DelChargingPackageResponse");
    private final static QName _EditChargingPackageResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "EditChargingPackageResponse");
    private final static QName _DelChargingPlanResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "DelChargingPlanResponse");
    private final static QName _GetChargingPlanDetailsResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "GetChargingPlanDetailsResponse");
    private final static QName _AddDestinationExceptionResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "AddDestinationExceptionResponse");
    private final static QName _GetDestinationExceptionsResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "GetDestinationExceptionsResponse");
    private final static QName _DelMonthlyLimitResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "DelMonthlyLimitResponse");
    private final static QName _DelDestinationExceptionResponse_QNAME = new QName("http://4psa.com/BillingMessages.xsd/2.5.1", "DelDestinationExceptionResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.billingmessages_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddChargingPlanRequest }
     * 
     */
    public AddChargingPlanRequest createAddChargingPlanRequest() {
        return new AddChargingPlanRequest();
    }

    /**
     * Create an instance of {@link GetRechargesResponse }
     * 
     */
    public GetRechargesResponse createGetRechargesResponse() {
        return new GetRechargesResponse();
    }

    /**
     * Create an instance of {@link AddChargingPlanRequest.Exception }
     * 
     */
    public AddChargingPlanRequest.Exception createAddChargingPlanRequestException() {
        return new AddChargingPlanRequest.Exception();
    }

    /**
     * Create an instance of {@link DelChargingPlanRequest }
     * 
     */
    public DelChargingPlanRequest createDelChargingPlanRequest() {
        return new DelChargingPlanRequest();
    }

    /**
     * Create an instance of {@link GetChargingPlanDetailsRequest }
     * 
     */
    public GetChargingPlanDetailsRequest createGetChargingPlanDetailsRequest() {
        return new GetChargingPlanDetailsRequest();
    }

    /**
     * Create an instance of {@link GetDestinationExceptionsRequest }
     * 
     */
    public GetDestinationExceptionsRequest createGetDestinationExceptionsRequest() {
        return new GetDestinationExceptionsRequest();
    }

    /**
     * Create an instance of {@link AddDestinationExceptionRequest }
     * 
     */
    public AddDestinationExceptionRequest createAddDestinationExceptionRequest() {
        return new AddDestinationExceptionRequest();
    }

    /**
     * Create an instance of {@link DelDestinationExceptionRequest }
     * 
     */
    public DelDestinationExceptionRequest createDelDestinationExceptionRequest() {
        return new DelDestinationExceptionRequest();
    }

    /**
     * Create an instance of {@link AddChargingPackageRequest }
     * 
     */
    public AddChargingPackageRequest createAddChargingPackageRequest() {
        return new AddChargingPackageRequest();
    }

    /**
     * Create an instance of {@link GetChargingPlansRequest }
     * 
     */
    public GetChargingPlansRequest createGetChargingPlansRequest() {
        return new GetChargingPlansRequest();
    }

    /**
     * Create an instance of {@link EditChargingPackageRequest }
     * 
     */
    public EditChargingPackageRequest createEditChargingPackageRequest() {
        return new EditChargingPackageRequest();
    }

    /**
     * Create an instance of {@link DelMonthlyLimitRequest }
     * 
     */
    public DelMonthlyLimitRequest createDelMonthlyLimitRequest() {
        return new DelMonthlyLimitRequest();
    }

    /**
     * Create an instance of {@link GetChargingPackagesRequest }
     * 
     */
    public GetChargingPackagesRequest createGetChargingPackagesRequest() {
        return new GetChargingPackagesRequest();
    }

    /**
     * Create an instance of {@link GetRechargesRequest }
     * 
     */
    public GetRechargesRequest createGetRechargesRequest() {
        return new GetRechargesRequest();
    }

    /**
     * Create an instance of {@link GetRechargesResponse.Recharge }
     * 
     */
    public GetRechargesResponse.Recharge createGetRechargesResponseRecharge() {
        return new GetRechargesResponse.Recharge();
    }

    /**
     * Create an instance of {@link EditDestinationExceptionRequest }
     * 
     */
    public EditDestinationExceptionRequest createEditDestinationExceptionRequest() {
        return new EditDestinationExceptionRequest();
    }

    /**
     * Create an instance of {@link EditChargingPlanResponse }
     * 
     */
    public EditChargingPlanResponse createEditChargingPlanResponse() {
        return new EditChargingPlanResponse();
    }

    /**
     * Create an instance of {@link EditChargingPlanRequest }
     * 
     */
    public EditChargingPlanRequest createEditChargingPlanRequest() {
        return new EditChargingPlanRequest();
    }

    /**
     * Create an instance of {@link DelChargingPackageRequest }
     * 
     */
    public DelChargingPackageRequest createDelChargingPackageRequest() {
        return new DelChargingPackageRequest();
    }

    /**
     * Create an instance of {@link RechargeRequest }
     * 
     */
    public RechargeRequest createRechargeRequest() {
        return new RechargeRequest();
    }

    /**
     * Create an instance of {@link AddChargingPlanResponse }
     * 
     */
    public AddChargingPlanResponse createAddChargingPlanResponse() {
        return new AddChargingPlanResponse();
    }

    /**
     * Create an instance of {@link AddChargingPlanRequest.Exception.Charge }
     * 
     */
    public AddChargingPlanRequest.Exception.Charge createAddChargingPlanRequestExceptionCharge() {
        return new AddChargingPlanRequest.Exception.Charge();
    }

    /**
     * Create an instance of {@link AddChargingPlanRequest.Exception.Package }
     * 
     */
    public AddChargingPlanRequest.Exception.Package createAddChargingPlanRequestExceptionPackage() {
        return new AddChargingPlanRequest.Exception.Package();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "EditDestinationExceptionResponse")
    public JAXBElement<UpdateObject> createEditDestinationExceptionResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_EditDestinationExceptionResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChargingPlanResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "GetChargingPlansResponse")
    public JAXBElement<GetChargingPlanResponseType> createGetChargingPlansResponse(GetChargingPlanResponseType value) {
        return new JAXBElement<GetChargingPlanResponseType>(_GetChargingPlansResponse_QNAME, GetChargingPlanResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChargingPackageList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "GetChargingPackagesResponse")
    public JAXBElement<ChargingPackageList> createGetChargingPackagesResponse(ChargingPackageList value) {
        return new JAXBElement<ChargingPackageList>(_GetChargingPackagesResponse_QNAME, ChargingPackageList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "RechargeResponse")
    public JAXBElement<UpdateObject> createRechargeResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_RechargeResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "AddChargingPackageResponse")
    public JAXBElement<UpdateObject> createAddChargingPackageResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddChargingPackageResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "DelChargingPackageResponse")
    public JAXBElement<DelObject> createDelChargingPackageResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelChargingPackageResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "EditChargingPackageResponse")
    public JAXBElement<UpdateObject> createEditChargingPackageResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_EditChargingPackageResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "DelChargingPlanResponse")
    public JAXBElement<DelObject> createDelChargingPlanResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelChargingPlanResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChargingPlanDetailsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "GetChargingPlanDetailsResponse")
    public JAXBElement<GetChargingPlanDetailsResponseType> createGetChargingPlanDetailsResponse(GetChargingPlanDetailsResponseType value) {
        return new JAXBElement<GetChargingPlanDetailsResponseType>(_GetChargingPlanDetailsResponse_QNAME, GetChargingPlanDetailsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "AddDestinationExceptionResponse")
    public JAXBElement<UpdateObject> createAddDestinationExceptionResponse(UpdateObject value) {
        return new JAXBElement<UpdateObject>(_AddDestinationExceptionResponse_QNAME, UpdateObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DestinationExceptionList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "GetDestinationExceptionsResponse")
    public JAXBElement<DestinationExceptionList> createGetDestinationExceptionsResponse(DestinationExceptionList value) {
        return new JAXBElement<DestinationExceptionList>(_GetDestinationExceptionsResponse_QNAME, DestinationExceptionList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "DelMonthlyLimitResponse")
    public JAXBElement<DelObject> createDelMonthlyLimitResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelMonthlyLimitResponse_QNAME, DelObject.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingMessages.xsd/2.5.1", name = "DelDestinationExceptionResponse")
    public JAXBElement<DelObject> createDelDestinationExceptionResponse(DelObject value) {
        return new JAXBElement<DelObject>(_DelDestinationExceptionResponse_QNAME, DelObject.class, null, value);
    }

}
