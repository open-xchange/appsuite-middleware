
package com._4psa.resellerdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.ClientInfo;
import com._4psa.resellermessages_xsd._2_5.AddResellerRequest;
import com._4psa.resellermessages_xsd._2_5.EditResellerRequest;


/**
 * Reseller account data
 * 
 * <p>Java class for ResellerInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResellerInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ClientInfo">
 *       &lt;choice>
 *         &lt;element name="channelRuleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResellerInfo", propOrder = {
    "channelRuleID",
    "chargingPlanID",
    "chargingPlanIdentifier"
})
@XmlSeeAlso({
    AddResellerRequest.class,
    EditResellerRequest.class
})
public class ResellerInfo
    extends ClientInfo
{

    protected BigInteger channelRuleID;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;

    /**
     * Gets the value of the channelRuleID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getChannelRuleID() {
        return channelRuleID;
    }

    /**
     * Sets the value of the channelRuleID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setChannelRuleID(BigInteger value) {
        this.channelRuleID = value;
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

    /**
     * Gets the value of the chargingPlanIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChargingPlanIdentifier() {
        return chargingPlanIdentifier;
    }

    /**
     * Sets the value of the chargingPlanIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChargingPlanIdentifier(String value) {
        this.chargingPlanIdentifier = value;
    }

}
