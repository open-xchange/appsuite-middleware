
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Size range type used for the WithinSizeRange rule predicate.
 * 
 * <p>Java class for RulePredicateSizeRangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RulePredicateSizeRangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MinimumSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MaximumSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RulePredicateSizeRangeType", propOrder = {
    "minimumSize",
    "maximumSize"
})
public class RulePredicateSizeRangeType {

    @XmlElement(name = "MinimumSize")
    protected Integer minimumSize;
    @XmlElement(name = "MaximumSize")
    protected Integer maximumSize;

    /**
     * Gets the value of the minimumSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinimumSize() {
        return minimumSize;
    }

    /**
     * Sets the value of the minimumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinimumSize(Integer value) {
        this.minimumSize = value;
    }

    /**
     * Gets the value of the maximumSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the value of the maximumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaximumSize(Integer value) {
        this.maximumSize = value;
    }

}
