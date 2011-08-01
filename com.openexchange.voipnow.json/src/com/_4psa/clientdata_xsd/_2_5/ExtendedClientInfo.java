
package com._4psa.clientdata_xsd._2_5;

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
import com._4psa.clientmessagesinfo_xsd._2_5.GetClientDetailsResponseType;
import com._4psa.resellerdata_xsd._2_5.ExtendedResellerInfo;


/**
 * Client account details data
 *
 * <p>Java class for ExtendedClientInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtendedClientInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ClientInfo">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="phoneStatus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="16"/>
 *               &lt;enumeration value="32"/>
 *               &lt;enumeration value="64"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="cpAccess" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="chargingPlan" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="parentName" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="link" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="resourceID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="UUID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
@XmlType(name = "ExtendedClientInfo", propOrder = {
    "id",
    "status",
    "phoneStatus",
    "cpAccess",
    "parentID",
    "parentIdentifier",
    "chargingPlanID",
    "chargingPlanIdentifier",
    "chargingPlan",
    "parentName",
    "templateID",
    "crDate",
    "identifier",
    "scope",
    "link",
    "accountFlag"
})
@XmlSeeAlso({
    GetClientDetailsResponseType.class,
    ExtendedResellerInfo.class
})
public class ExtendedClientInfo
    extends ClientInfo
{

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    protected Boolean status;
    @XmlElement(defaultValue = "1")
    protected String phoneStatus;
    protected Boolean cpAccess;
    protected BigInteger parentID;
    protected String parentIdentifier;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;
    protected String chargingPlan;
    protected String parentName;
    protected BigInteger templateID;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar crDate;
    protected String identifier;
    protected String scope;
    protected List<ExtendedClientInfo.Link> link;
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
     * Gets the value of the phoneStatus property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhoneStatus() {
        return phoneStatus;
    }

    /**
     * Sets the value of the phoneStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhoneStatus(String value) {
        this.phoneStatus = value;
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
     * Gets the value of the link property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLink().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExtendedClientInfo.Link }
     *
     *
     */
    public List<ExtendedClientInfo.Link> getLink() {
        if (link == null) {
            link = new ArrayList<ExtendedClientInfo.Link>();
        }
        return this.link;
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


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="resourceID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="UUID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "resourceID",
        "uuid"
    })
    public static class Link {

        @XmlElement(defaultValue = "1")
        protected BigInteger resourceID;
        @XmlElement(name = "UUID")
        protected String uuid;

        /**
         * Gets the value of the resourceID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getResourceID() {
            return resourceID;
        }

        /**
         * Sets the value of the resourceID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setResourceID(BigInteger value) {
            this.resourceID = value;
        }

        /**
         * Gets the value of the uuid property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getUUID() {
            return uuid;
        }

        /**
         * Sets the value of the uuid property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setUUID(String value) {
            this.uuid = value;
        }

    }

}
