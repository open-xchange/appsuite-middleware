
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com._4psa.common_xsd._2_5.UnlimitedUFloat;


/**
 * Calling card code list data
 * 
 * <p>Java class for CardCodeList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CardCodeList">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CardCodeInfo">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
 *         &lt;element name="recharges" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="availableCredit" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardCodeList", propOrder = {
    "id",
    "status",
    "recharges",
    "availableCredit",
    "crDate"
})
public class CardCodeList
    extends CardCodeInfo
{

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    protected boolean status;
    protected BigInteger recharges;
    protected UnlimitedUFloat availableCredit;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar crDate;

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
     * Gets the value of the recharges property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRecharges() {
        return recharges;
    }

    /**
     * Sets the value of the recharges property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRecharges(BigInteger value) {
        this.recharges = value;
    }

    /**
     * Gets the value of the availableCredit property.
     * 
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *     
     */
    public UnlimitedUFloat getAvailableCredit() {
        return availableCredit;
    }

    /**
     * Sets the value of the availableCredit property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *     
     */
    public void setAvailableCredit(UnlimitedUFloat value) {
        this.availableCredit = value;
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

}
