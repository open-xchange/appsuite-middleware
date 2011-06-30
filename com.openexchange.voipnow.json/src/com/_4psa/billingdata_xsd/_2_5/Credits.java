
package com._4psa.billingdata_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Charging credits definition: prepaid charging plan
 * 
 * <p>Java class for Credits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Credits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="creditIn" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="creditOut" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
@XmlType(name = "Credits", propOrder = {
    "creditIn",
    "creditOut",
    "orderNo"
})
@XmlSeeAlso({
    CreditsList.class
})
public class Credits {

    @XmlElementRef(name = "creditIn", namespace = "http://4psa.com/BillingData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<String> creditIn;
    protected String creditOut;
    protected String orderNo;

    /**
     * Gets the value of the creditIn property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreditIn() {
        return creditIn;
    }

    /**
     * Sets the value of the creditIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreditIn(JAXBElement<String> value) {
        this.creditIn = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the creditOut property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditOut() {
        return creditOut;
    }

    /**
     * Sets the value of the creditOut property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditOut(String value) {
        this.creditOut = value;
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
