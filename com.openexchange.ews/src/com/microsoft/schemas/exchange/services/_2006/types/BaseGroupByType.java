
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseGroupByType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseGroupByType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Order" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}SortDirectionType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseGroupByType")
@XmlSeeAlso({
    GroupByType.class,
    DistinguishedGroupByType.class
})
public abstract class BaseGroupByType {

    @XmlAttribute(name = "Order", required = true)
    protected SortDirectionType order;

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link SortDirectionType }
     *     
     */
    public SortDirectionType getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link SortDirectionType }
     *     
     */
    public void setOrder(SortDirectionType value) {
        this.order = value;
    }

}
