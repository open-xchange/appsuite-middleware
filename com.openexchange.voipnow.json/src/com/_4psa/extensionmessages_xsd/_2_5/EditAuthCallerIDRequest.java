
package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="CallerID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="PIN" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
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
    "id",
    "callerID",
    "pin",
    "status"
})
@XmlRootElement(name = "EditAuthCallerIDRequest")
public class EditAuthCallerIDRequest {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    @XmlElement(name = "CallerID")
    protected String callerID;
    @XmlElement(name = "PIN")
    protected BigDecimal pin;
    protected BigInteger status;

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
     * Gets the value of the callerID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallerID() {
        return callerID;
    }

    /**
     * Sets the value of the callerID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallerID(String value) {
        this.callerID = value;
    }

    /**
     * Gets the value of the pin property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getPIN() {
        return pin;
    }

    /**
     * Sets the value of the pin property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setPIN(BigDecimal value) {
        this.pin = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setStatus(BigInteger value) {
        this.status = value;
    }

}
