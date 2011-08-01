
package com._4psa.billingmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingdata_xsd._2_5.ChargingPackage;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}ChargingPackage">
 *       &lt;sequence>
 *         &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="chargingPackageID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "chargingPlanID",
    "chargingPackageID"
})
@XmlRootElement(name = "EditChargingPackageRequest")
public class EditChargingPackageRequest
    extends ChargingPackage
{

    @XmlElement(required = true)
    protected BigInteger chargingPlanID;
    @XmlElement(required = true)
    protected BigInteger chargingPackageID;

    /**
     * Gets the value of the chargingPlanID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargingPlanID() {
        return chargingPlanID;
    }

    /**
     * Sets the value of the chargingPlanID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargingPlanID(BigInteger value) {
        this.chargingPlanID = value;
    }

    /**
     * Gets the value of the chargingPackageID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargingPackageID() {
        return chargingPackageID;
    }

    /**
     * Sets the value of the chargingPackageID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargingPackageID(BigInteger value) {
        this.chargingPackageID = value;
    }

}
