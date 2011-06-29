
package com._4psa.reportmessages_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com._4psa.reportmessagesinfo_xsd._2_5.CallCostsResponseType;
import com._4psa.reportmessagesinfo_xsd._2_5.CallReportResponseType;
import com._4psa.reportmessagesinfo_xsd._2_5.QuickStatsResponseType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.reportmessages_xsd._2_5 package. 
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

    private final static QName _CallReportResponse_QNAME = new QName("http://4psa.com/ReportMessages.xsd/2.5.1", "CallReportResponse");
    private final static QName _CallCostsResponse_QNAME = new QName("http://4psa.com/ReportMessages.xsd/2.5.1", "CallCostsResponse");
    private final static QName _QuickStatsResponse_QNAME = new QName("http://4psa.com/ReportMessages.xsd/2.5.1", "QuickStatsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.reportmessages_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CallReportRequest }
     * 
     */
    public CallReportRequest createCallReportRequest() {
        return new CallReportRequest();
    }

    /**
     * Create an instance of {@link CallCostsRequest }
     * 
     */
    public CallCostsRequest createCallCostsRequest() {
        return new CallCostsRequest();
    }

    /**
     * Create an instance of {@link CallReportRequest.Interval }
     * 
     */
    public CallReportRequest.Interval createCallReportRequestInterval() {
        return new CallReportRequest.Interval();
    }

    /**
     * Create an instance of {@link QuickStatsRequest }
     * 
     */
    public QuickStatsRequest createQuickStatsRequest() {
        return new QuickStatsRequest();
    }

    /**
     * Create an instance of {@link CallCostsRequest.Interval }
     * 
     */
    public CallCostsRequest.Interval createCallCostsRequestInterval() {
        return new CallCostsRequest.Interval();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CallReportResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ReportMessages.xsd/2.5.1", name = "CallReportResponse")
    public JAXBElement<CallReportResponseType> createCallReportResponse(CallReportResponseType value) {
        return new JAXBElement<CallReportResponseType>(_CallReportResponse_QNAME, CallReportResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CallCostsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ReportMessages.xsd/2.5.1", name = "CallCostsResponse")
    public JAXBElement<CallCostsResponseType> createCallCostsResponse(CallCostsResponseType value) {
        return new JAXBElement<CallCostsResponseType>(_CallCostsResponse_QNAME, CallCostsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QuickStatsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ReportMessages.xsd/2.5.1", name = "QuickStatsResponse")
    public JAXBElement<QuickStatsResponseType> createQuickStatsResponse(QuickStatsResponseType value) {
        return new JAXBElement<QuickStatsResponseType>(_QuickStatsResponse_QNAME, QuickStatsResponseType.class, null, value);
    }

}
