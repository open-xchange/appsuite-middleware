
package com._4psa.clientmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.ClientInfo;
import com._4psa.common_xsd._2_5.AdvertisingTemplate;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ClientInfo">
 *       &lt;sequence>
 *         &lt;element name="channelRuleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="industry" type="{http://4psa.com/Common.xsd/2.5.1}rule" minOccurs="0"/>
 *         &lt;element name="advertisingTemplate" type="{http://4psa.com/Common.xsd/2.5.1}advertisingTemplate" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="fromUser" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="fromUserIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
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
    "channelRuleID",
    "industry",
    "advertisingTemplate",
    "id",
    "identifier",
    "chargingPlanID",
    "chargingPlanIdentifier",
    "fromUser",
    "fromUserIdentifier"
})
@XmlRootElement(name = "EditClientRequest")
public class EditClientRequest
    extends ClientInfo
{

    protected BigInteger channelRuleID;
    @XmlElement(defaultValue = "0")
    protected String industry;
    protected AdvertisingTemplate advertisingTemplate;
    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;
    protected BigInteger fromUser;
    protected String fromUserIdentifier;

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
     * Gets the value of the industry property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIndustry() {
        return industry;
    }

    /**
     * Sets the value of the industry property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIndustry(String value) {
        this.industry = value;
    }

    /**
     * Gets the value of the advertisingTemplate property.
     *
     * @return
     *     possible object is
     *     {@link AdvertisingTemplate }
     *
     */
    public AdvertisingTemplate getAdvertisingTemplate() {
        return advertisingTemplate;
    }

    /**
     * Sets the value of the advertisingTemplate property.
     *
     * @param value
     *     allowed object is
     *     {@link AdvertisingTemplate }
     *
     */
    public void setAdvertisingTemplate(AdvertisingTemplate value) {
        this.advertisingTemplate = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the identifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIdentifier(String value) {
        this.identifier = value;
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

    /**
     * Gets the value of the fromUser property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getFromUser() {
        return fromUser;
    }

    /**
     * Sets the value of the fromUser property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setFromUser(BigInteger value) {
        this.fromUser = value;
    }

    /**
     * Gets the value of the fromUserIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFromUserIdentifier() {
        return fromUserIdentifier;
    }

    /**
     * Sets the value of the fromUserIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFromUserIdentifier(String value) {
        this.fromUserIdentifier = value;
    }

}
