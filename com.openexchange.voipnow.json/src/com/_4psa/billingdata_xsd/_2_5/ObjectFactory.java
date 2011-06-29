
package com._4psa.billingdata_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com._4psa.billingdata_xsd._2_5 package. 
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

    private final static QName _CreditsCreditIn_QNAME = new QName("http://4psa.com/BillingData.xsd/2.5.1", "creditIn");
    private final static QName _LimitsLimitIn_QNAME = new QName("http://4psa.com/BillingData.xsd/2.5.1", "limitIn");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com._4psa.billingdata_xsd._2_5
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChargingPlanInfo }
     * 
     */
    public ChargingPlanInfo createChargingPlanInfo() {
        return new ChargingPlanInfo();
    }

    /**
     * Create an instance of {@link DestinationException }
     * 
     */
    public DestinationException createDestinationException() {
        return new DestinationException();
    }

    /**
     * Create an instance of {@link DestinationExceptionList }
     * 
     */
    public DestinationExceptionList createDestinationExceptionList() {
        return new DestinationExceptionList();
    }

    /**
     * Create an instance of {@link ChargingPackageList }
     * 
     */
    public ChargingPackageList createChargingPackageList() {
        return new ChargingPackageList();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.InheritedCharge }
     * 
     */
    public ChargingPlanInfo.InheritedCharge createChargingPlanInfoInheritedCharge() {
        return new ChargingPlanInfo.InheritedCharge();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.FixedCharge }
     * 
     */
    public ChargingPlanInfo.FixedCharge createChargingPlanInfoFixedCharge() {
        return new ChargingPlanInfo.FixedCharge();
    }

    /**
     * Create an instance of {@link ChargingPackage }
     * 
     */
    public ChargingPackage createChargingPackage() {
        return new ChargingPackage();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.ExternalMin }
     * 
     */
    public ChargingPlanInfo.ExternalMin createChargingPlanInfoExternalMin() {
        return new ChargingPlanInfo.ExternalMin();
    }

    /**
     * Create an instance of {@link DestinationException.Charge }
     * 
     */
    public DestinationException.Charge createDestinationExceptionCharge() {
        return new DestinationException.Charge();
    }

    /**
     * Create an instance of {@link DestinationException.Package }
     * 
     */
    public DestinationException.Package createDestinationExceptionPackage() {
        return new DestinationException.Package();
    }

    /**
     * Create an instance of {@link Limits }
     * 
     */
    public Limits createLimits() {
        return new Limits();
    }

    /**
     * Create an instance of {@link Credits }
     * 
     */
    public Credits createCredits() {
        return new Credits();
    }

    /**
     * Create an instance of {@link LimitsList }
     * 
     */
    public LimitsList createLimitsList() {
        return new LimitsList();
    }

    /**
     * Create an instance of {@link CreditsList }
     * 
     */
    public CreditsList createCreditsList() {
        return new CreditsList();
    }

    /**
     * Create an instance of {@link ChargingPlanList }
     * 
     */
    public ChargingPlanList createChargingPlanList() {
        return new ChargingPlanList();
    }

    /**
     * Create an instance of {@link DestinationExceptionList.Exception }
     * 
     */
    public DestinationExceptionList.Exception createDestinationExceptionListException() {
        return new DestinationExceptionList.Exception();
    }

    /**
     * Create an instance of {@link ChargingPackageList.Package }
     * 
     */
    public ChargingPackageList.Package createChargingPackageListPackage() {
        return new ChargingPackageList.Package();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.InheritedCharge.ExternalIncoming }
     * 
     */
    public ChargingPlanInfo.InheritedCharge.ExternalIncoming createChargingPlanInfoInheritedChargeExternalIncoming() {
        return new ChargingPlanInfo.InheritedCharge.ExternalIncoming();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.InheritedCharge.External }
     * 
     */
    public ChargingPlanInfo.InheritedCharge.External createChargingPlanInfoInheritedChargeExternal() {
        return new ChargingPlanInfo.InheritedCharge.External();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.InheritedCharge.Local }
     * 
     */
    public ChargingPlanInfo.InheritedCharge.Local createChargingPlanInfoInheritedChargeLocal() {
        return new ChargingPlanInfo.InheritedCharge.Local();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.InheritedCharge.Extended }
     * 
     */
    public ChargingPlanInfo.InheritedCharge.Extended createChargingPlanInfoInheritedChargeExtended() {
        return new ChargingPlanInfo.InheritedCharge.Extended();
    }

    /**
     * Create an instance of {@link ChargingPlanInfo.FixedCharge.External }
     * 
     */
    public ChargingPlanInfo.FixedCharge.External createChargingPlanInfoFixedChargeExternal() {
        return new ChargingPlanInfo.FixedCharge.External();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingData.xsd/2.5.1", name = "creditIn", scope = Credits.class)
    public JAXBElement<String> createCreditsCreditIn(String value) {
        return new JAXBElement<String>(_CreditsCreditIn_QNAME, String.class, Credits.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://4psa.com/BillingData.xsd/2.5.1", name = "limitIn", scope = Limits.class)
    public JAXBElement<String> createLimitsLimitIn(String value) {
        return new JAXBElement<String>(_LimitsLimitIn_QNAME, String.class, Limits.class, value);
    }

}
