
package com._4psa.billingdata_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Charging limits definition: postpaid charging plan
 *
 * <p>Java class for Limits complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Limits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="limitIn" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="limitOut" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="overusage" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="orderNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="monthly" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Limits", propOrder = {
    "limitIn",
    "limitOut",
    "overusage",
    "orderNo",
    "monthly"
})
@XmlSeeAlso({
    LimitsList.class
})
public class Limits {

    @XmlElementRef(name = "limitIn", namespace = "http://4psa.com/BillingData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<String> limitIn;
    protected String limitOut;
    protected String overusage;
    protected String orderNo;
    protected Boolean monthly;

    /**
     * Gets the value of the limitIn property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getLimitIn() {
        return limitIn;
    }

    /**
     * Sets the value of the limitIn property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public void setLimitIn(JAXBElement<String> value) {
        this.limitIn = value;
    }

    /**
     * Gets the value of the limitOut property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLimitOut() {
        return limitOut;
    }

    /**
     * Sets the value of the limitOut property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLimitOut(String value) {
        this.limitOut = value;
    }

    /**
     * Gets the value of the overusage property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOverusage() {
        return overusage;
    }

    /**
     * Sets the value of the overusage property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOverusage(String value) {
        this.overusage = value;
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

    /**
     * Gets the value of the monthly property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMonthly() {
        return monthly;
    }

    /**
     * Sets the value of the monthly property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMonthly(Boolean value) {
        this.monthly = value;
    }

}
