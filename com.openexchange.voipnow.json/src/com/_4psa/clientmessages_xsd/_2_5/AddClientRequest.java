
package com._4psa.clientmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="industry" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="advertisingTemplate" type="{http://4psa.com/Common.xsd/2.5.1}advertisingTemplate" minOccurs="0"/>
 *         &lt;element name="channelRuleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="parentLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="fromUser" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="fromUserIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="verbose" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="notifyOnly" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="linkResourceID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="linkUUID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="dku" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="accountFlag" maxOccurs="unbounded" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="noneditablelogin"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
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
    "templateID",
    "industry",
    "advertisingTemplate",
    "channelRuleID",
    "identifier",
    "parentID",
    "parentIdentifier",
    "parentLogin",
    "chargingPlanID",
    "chargingPlanIdentifier",
    "fromUser",
    "fromUserIdentifier",
    "verbose",
    "notifyOnly",
    "scope",
    "linkResourceID",
    "linkUUID",
    "dku",
    "accountFlag"
})
@XmlRootElement(name = "AddClientRequest")
public class AddClientRequest
    extends ClientInfo
{

    protected BigInteger templateID;
    @XmlElement(defaultValue = "0")
    protected String industry;
    protected AdvertisingTemplate advertisingTemplate;
    protected BigInteger channelRuleID;
    protected String identifier;
    protected BigInteger parentID;
    protected String parentIdentifier;
    protected String parentLogin;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;
    protected BigInteger fromUser;
    protected String fromUserIdentifier;
    @XmlElement(defaultValue = "0")
    protected Boolean verbose;
    protected String notifyOnly;
    protected String scope;
    @XmlElement(defaultValue = "1")
    protected BigInteger linkResourceID;
    protected String linkUUID;
    protected String dku;
    @XmlElement(defaultValue = "noneditablelogin")
    protected List<String> accountFlag;

    /**
     * Gets the value of the templateID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTemplateID() {
        return templateID;
    }

    /**
     * Sets the value of the templateID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTemplateID(BigInteger value) {
        this.templateID = value;
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
     * Gets the value of the parentID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getParentID() {
        return parentID;
    }

    /**
     * Sets the value of the parentID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setParentID(BigInteger value) {
        this.parentID = value;
    }

    /**
     * Gets the value of the parentIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Sets the value of the parentIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParentIdentifier(String value) {
        this.parentIdentifier = value;
    }

    /**
     * Gets the value of the parentLogin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParentLogin() {
        return parentLogin;
    }

    /**
     * Sets the value of the parentLogin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParentLogin(String value) {
        this.parentLogin = value;
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

    /**
     * Gets the value of the verbose property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the value of the verbose property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setVerbose(Boolean value) {
        this.verbose = value;
    }

    /**
     * Gets the value of the notifyOnly property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNotifyOnly() {
        return notifyOnly;
    }

    /**
     * Sets the value of the notifyOnly property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNotifyOnly(String value) {
        this.notifyOnly = value;
    }

    /**
     * Gets the value of the scope property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the linkResourceID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getLinkResourceID() {
        return linkResourceID;
    }

    /**
     * Sets the value of the linkResourceID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setLinkResourceID(BigInteger value) {
        this.linkResourceID = value;
    }

    /**
     * Gets the value of the linkUUID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLinkUUID() {
        return linkUUID;
    }

    /**
     * Sets the value of the linkUUID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLinkUUID(String value) {
        this.linkUUID = value;
    }

    /**
     * Gets the value of the dku property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDku() {
        return dku;
    }

    /**
     * Sets the value of the dku property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDku(String value) {
        this.dku = value;
    }

    /**
     * Gets the value of the accountFlag property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accountFlag property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccountFlag().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getAccountFlag() {
        if (accountFlag == null) {
            accountFlag = new ArrayList<String>();
        }
        return this.accountFlag;
    }

}
