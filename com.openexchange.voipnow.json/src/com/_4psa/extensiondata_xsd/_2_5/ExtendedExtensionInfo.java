
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetExtensionDetailsResponseType;


/**
 * Extension account details data
 *
 * <p>Java class for ExtendedExtensionInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtendedExtensionInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtensionInfo">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="extensionID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="cpAccess" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="parentName" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="chargingPlan" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
@XmlType(name = "ExtendedExtensionInfo", propOrder = {
    "id",
    "identifier",
    "extensionID",
    "parentID",
    "parentIdentifier",
    "status",
    "cpAccess",
    "parentName",
    "chargingPlan",
    "templateID",
    "crDate",
    "scope",
    "accountFlag"
})
@XmlSeeAlso({
    GetExtensionDetailsResponseType.class
})
public class ExtendedExtensionInfo
    extends ExtensionInfo
{

    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;
    protected BigInteger extensionID;
    protected BigInteger parentID;
    protected String parentIdentifier;
    protected Boolean status;
    protected Boolean cpAccess;
    protected String parentName;
    protected String chargingPlan;
    protected BigInteger templateID;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar crDate;
    protected String scope;
    @XmlElement(defaultValue = "noneditablelogin")
    protected List<String> accountFlag;

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
     * Gets the value of the extensionID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getExtensionID() {
        return extensionID;
    }

    /**
     * Sets the value of the extensionID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setExtensionID(BigInteger value) {
        this.extensionID = value;
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
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setStatus(Boolean value) {
        this.status = value;
    }

    /**
     * Gets the value of the cpAccess property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCpAccess() {
        return cpAccess;
    }

    /**
     * Sets the value of the cpAccess property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCpAccess(Boolean value) {
        this.cpAccess = value;
    }

    /**
     * Gets the value of the parentName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParentName() {
        return parentName;
    }

    /**
     * Sets the value of the parentName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParentName(String value) {
        this.parentName = value;
    }

    /**
     * Gets the value of the chargingPlan property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getChargingPlan() {
        return chargingPlan;
    }

    /**
     * Sets the value of the chargingPlan property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setChargingPlan(String value) {
        this.chargingPlan = value;
    }

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
     * Gets the value of the crDate property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getCrDate() {
        return crDate;
    }

    /**
     * Sets the value of the crDate property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setCrDate(XMLGregorianCalendar value) {
        this.crDate = value;
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
