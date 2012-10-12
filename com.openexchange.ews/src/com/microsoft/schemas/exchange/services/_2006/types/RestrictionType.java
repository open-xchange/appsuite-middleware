
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RestrictionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RestrictionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}SearchExpression"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RestrictionType", propOrder = {
    "searchExpression"
})
public class RestrictionType {

    @XmlElementRef(name = "SearchExpression", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class)
    protected JAXBElement<? extends SearchExpressionType> searchExpression;

    /**
     * Gets the value of the searchExpression property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link IsLessThanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsNotEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SearchExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ContainsExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsGreaterThanOrEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsGreaterThanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link OrType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExcludesType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExistsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsLessThanOrEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NotType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AndType }{@code >}
     *     
     */
    public JAXBElement<? extends SearchExpressionType> getSearchExpression() {
        return searchExpression;
    }

    /**
     * Sets the value of the searchExpression property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link IsLessThanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsNotEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SearchExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ContainsExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsGreaterThanOrEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsGreaterThanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link OrType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExcludesType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ExistsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link IsLessThanOrEqualToType }{@code >}
     *     {@link JAXBElement }{@code <}{@link NotType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AndType }{@code >}
     *     
     */
    public void setSearchExpression(JAXBElement<? extends SearchExpressionType> value) {
        this.searchExpression = ((JAXBElement<? extends SearchExpressionType> ) value);
    }

}
