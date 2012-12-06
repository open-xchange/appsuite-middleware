
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TransitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="To" type="{http://schemas.microsoft.com/exchange/services/2006/types}TransitionTargetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransitionType", propOrder = {
    "to"
})
@XmlSeeAlso({
    AbsoluteDateTransitionType.class,
    RecurringTimeTransitionType.class
})
public class TransitionType {

    @XmlElement(name = "To", required = true)
    protected TransitionTargetType to;

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link TransitionTargetType }
     *     
     */
    public TransitionTargetType getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransitionTargetType }
     *     
     */
    public void setTo(TransitionTargetType value) {
        this.to = value;
    }

}
