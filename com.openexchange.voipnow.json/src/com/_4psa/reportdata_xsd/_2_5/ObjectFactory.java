
package com._4psa.reportdata_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.reportdata_xsd._2_5 package. 
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

    private final static QName _CallCostInfoProfit_QNAME = new QName("http://4psa.com/ReportData.xsd/2.5.1", "profit");
    private final static QName _CallCostInfoCost_QNAME = new QName("http://4psa.com/ReportData.xsd/2.5.1", "cost");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.reportdata_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link QuickStats }
     * 
     */
    public QuickStats createQuickStats() {
        return new QuickStats();
    }

    /**
     * Create an instance of {@link CallReport }
     * 
     */
    public CallReport createCallReport() {
        return new CallReport();
    }

    /**
     * Create an instance of {@link CallCostInfo }
     * 
     */
    public CallCostInfo createCallCostInfo() {
        return new CallCostInfo();
    }

    /**
     * Create an instance of {@link CallStatistics }
     * 
     */
    public CallStatistics createCallStatistics() {
        return new CallStatistics();
    }

    /**
     * Create an instance of {@link UserStatistics }
     * 
     */
    public UserStatistics createUserStatistics() {
        return new UserStatistics();
    }

    /**
     * Create an instance of {@link QuickStats.Extensions }
     * 
     */
    public QuickStats.Extensions createQuickStatsExtensions() {
        return new QuickStats.Extensions();
    }

    /**
     * Create an instance of {@link CallReport.IncomingCalls }
     * 
     */
    public CallReport.IncomingCalls createCallReportIncomingCalls() {
        return new CallReport.IncomingCalls();
    }

    /**
     * Create an instance of {@link CallReport.OutgoingCalls }
     * 
     */
    public CallReport.OutgoingCalls createCallReportOutgoingCalls() {
        return new CallReport.OutgoingCalls();
    }

    /**
     * Create an instance of {@link CallReport.Call }
     * 
     */
    public CallReport.Call createCallReportCall() {
        return new CallReport.Call();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ReportData.xsd/2.5.1", name = "profit", scope = CallCostInfo.class)
    public JAXBElement<Float> createCallCostInfoProfit(Float value) {
        return new JAXBElement<Float>(_CallCostInfoProfit_QNAME, Float.class, CallCostInfo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/ReportData.xsd/2.5.1", name = "cost", scope = CallCostInfo.class)
    public JAXBElement<Float> createCallCostInfoCost(Float value) {
        return new JAXBElement<Float>(_CallCostInfoCost_QNAME, Float.class, CallCostInfo.class, value);
    }

}
