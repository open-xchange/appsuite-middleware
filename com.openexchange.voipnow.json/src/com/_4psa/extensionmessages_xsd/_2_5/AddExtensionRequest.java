
package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.ExtensionInfo;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtensionInfo">
 *       &lt;sequence>
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="extensionNo" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="extensionType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="term"/>
 *               &lt;enumeration value="queue"/>
 *               &lt;enumeration value="ivr"/>
 *               &lt;enumeration value="voicecenter"/>
 *               &lt;enumeration value="conference"/>
 *               &lt;enumeration value="callback"/>
 *               &lt;enumeration value="callcard"/>
 *               &lt;enumeration value="intercom"/>
 *               &lt;enumeration value="queuecenter"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="parentLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="fromUser" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="fromUserIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="verbose" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="notifyOnly" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
    "extensionNo",
    "extensionType",
    "parentID",
    "parentIdentifier",
    "parentLogin",
    "fromUser",
    "fromUserIdentifier",
    "chargingPlanID",
    "chargingPlanIdentifier",
    "verbose",
    "notifyOnly",
    "scope",
    "dku",
    "accountFlag"
})
@XmlRootElement(name = "AddExtensionRequest")
public class AddExtensionRequest
    extends ExtensionInfo
{

    protected BigInteger templateID;
    protected BigInteger extensionNo;
    @XmlElement(defaultValue = "term")
    protected String extensionType;
    protected BigInteger parentID;
    protected String parentIdentifier;
    protected String parentLogin;
    protected BigInteger fromUser;
    protected String fromUserIdentifier;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;
    @XmlElement(defaultValue = "0")
    protected Boolean verbose;
    protected String notifyOnly;
    protected String scope;
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
     * Gets the value of the extensionNo property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getExtensionNo() {
        return extensionNo;
    }

    /**
     * Sets the value of the extensionNo property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setExtensionNo(BigInteger value) {
        this.extensionNo = value;
    }

    /**
     * Gets the value of the extensionType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtensionType() {
        return extensionType;
    }

    /**
     * Sets the value of the extensionType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtensionType(String value) {
        this.extensionType = value;
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
