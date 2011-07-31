
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUFloat;


/**
 * Authorized Caller ID data
 *
 * <p>Java class for CallerIDInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CallerIDInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CallerID" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="PIN" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="credit" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;element name="orderNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallerIDInfo", propOrder = {
    "callerID",
    "pin",
    "credit",
    "orderNo"
})
@XmlSeeAlso({
    CallerIDList.class
})
public class CallerIDInfo {

    @XmlElement(name = "CallerID", required = true)
    protected String callerID;
    @XmlElement(name = "PIN")
    protected BigDecimal pin;
    protected UnlimitedUFloat credit;
    protected String orderNo;

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
     * Gets the value of the credit property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getCredit() {
        return credit;
    }

    /**
     * Sets the value of the credit property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setCredit(UnlimitedUFloat value) {
        this.credit = value;
    }

    /**
     * Gets the value of the orderNo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * Sets the value of the orderNo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOrderNo(String value) {
        this.orderNo = value;
    }

}
