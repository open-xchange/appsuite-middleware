
package com._4psa.channeldata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Outgoing routing rules group list
 * 
 * <p>Java class for CallRulesOutGroupList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CallRulesOutGroupList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
 *         &lt;element name="rulesNo" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt"/>
 *         &lt;element name="inUse" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="default" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallRulesOutGroupList", propOrder = {
    "id",
    "name",
    "status",
    "rulesNo",
    "inUse",
    "crDate",
    "_default"
})
public class CallRulesOutGroupList {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    @XmlElement(required = true)
    protected String name;
    protected boolean status;
    protected long rulesNo;
    protected Boolean inUse;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar crDate;
    @XmlElement(name = "default")
    protected Boolean _default;

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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the status property.
     * 
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     */
    public void setStatus(boolean value) {
        this.status = value;
    }

    /**
     * Gets the value of the rulesNo property.
     * 
     */
    public long getRulesNo() {
        return rulesNo;
    }

    /**
     * Sets the value of the rulesNo property.
     * 
     */
    public void setRulesNo(long value) {
        this.rulesNo = value;
    }

    /**
     * Gets the value of the inUse property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInUse() {
        return inUse;
    }

    /**
     * Sets the value of the inUse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInUse(Boolean value) {
        this.inUse = value;
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
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

}
