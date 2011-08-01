
package com._4psa.common_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Increase/decrease with limit for PL
 *
 * <p>Java class for limit complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="limit">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="value" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *         &lt;element name="increase" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="decrease" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="unlimited" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "limit", propOrder = {
    "value",
    "increase",
    "decrease",
    "unlimited"
})
public class Limit {

    protected BigInteger value;
    protected String increase;
    protected String decrease;
    protected Boolean unlimited;

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * Gets the value of the increase property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIncrease() {
        return increase;
    }

    /**
     * Sets the value of the increase property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIncrease(String value) {
        this.increase = value;
    }

    /**
     * Gets the value of the decrease property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDecrease() {
        return decrease;
    }

    /**
     * Sets the value of the decrease property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDecrease(String value) {
        this.decrease = value;
    }

    /**
     * Gets the value of the unlimited property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUnlimited() {
        return unlimited;
    }

    /**
     * Sets the value of the unlimited property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUnlimited(Boolean value) {
        this.unlimited = value;
    }

}
