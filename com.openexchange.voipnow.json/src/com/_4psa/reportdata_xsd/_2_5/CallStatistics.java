
package com._4psa.reportdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Call statistics data
 *
 * <p>Java class for CallStatistics complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CallStatistics">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="duration" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *         &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *         &lt;element name="profit" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallStatistics", propOrder = {
    "total",
    "duration",
    "cost",
    "profit"
})
public class CallStatistics {

    protected BigInteger total;
    protected Float duration;
    protected Float cost;
    protected Float profit;

    /**
     * Gets the value of the total property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTotal(BigInteger value) {
        this.total = value;
    }

    /**
     * Gets the value of the duration property.
     *
     * @return
     *     possible object is
     *     {@link Float }
     *
     */
    public Float getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     *
     * @param value
     *     allowed object is
     *     {@link Float }
     *
     */
    public void setDuration(Float value) {
        this.duration = value;
    }

    /**
     * Gets the value of the cost property.
     *
     * @return
     *     possible object is
     *     {@link Float }
     *
     */
    public Float getCost() {
        return cost;
    }

    /**
     * Sets the value of the cost property.
     *
     * @param value
     *     allowed object is
     *     {@link Float }
     *
     */
    public void setCost(Float value) {
        this.cost = value;
    }

    /**
     * Gets the value of the profit property.
     *
     * @return
     *     possible object is
     *     {@link Float }
     *
     */
    public Float getProfit() {
        return profit;
    }

    /**
     * Sets the value of the profit property.
     *
     * @param value
     *     allowed object is
     *     {@link Float }
     *
     */
    public void setProfit(Float value) {
        this.profit = value;
    }

}
