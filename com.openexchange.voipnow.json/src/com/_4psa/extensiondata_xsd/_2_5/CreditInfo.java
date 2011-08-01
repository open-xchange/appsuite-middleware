
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Calling card code/Authorized Caller-ID credit
 *
 * <p>Java class for CreditInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CreditInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="credit" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="orderNo" type="{http://4psa.com/Common.xsd/2.5.1}text" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreditInfo", propOrder = {
    "id",
    "credit",
    "orderNo"
})
public class CreditInfo {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    protected float credit;
    protected String orderNo;

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
     * Gets the value of the credit property.
     *
     */
    public float getCredit() {
        return credit;
    }

    /**
     * Sets the value of the credit property.
     *
     */
    public void setCredit(float value) {
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
