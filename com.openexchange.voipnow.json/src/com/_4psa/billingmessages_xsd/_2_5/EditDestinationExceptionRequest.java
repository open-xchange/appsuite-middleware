
package com._4psa.billingmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingdata_xsd._2_5.DestinationException;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}DestinationException">
 *       &lt;sequence>
 *         &lt;element name="exceptionID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
    "exceptionID",
    "chargingPlanID"
})
@XmlRootElement(name = "EditDestinationExceptionRequest")
public class EditDestinationExceptionRequest
    extends DestinationException
{

    @XmlElement(required = true)
    protected BigInteger exceptionID;
    @XmlElement(required = true)
    protected BigInteger chargingPlanID;

    /**
     * Gets the value of the exceptionID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getExceptionID() {
        return exceptionID;
    }

    /**
     * Sets the value of the exceptionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setExceptionID(BigInteger value) {
        this.exceptionID = value;
    }

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

}
