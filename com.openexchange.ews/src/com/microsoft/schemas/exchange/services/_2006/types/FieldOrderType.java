
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FieldOrderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FieldOrderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}Path"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Order" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}SortDirectionType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldOrderType", propOrder = {
    "path"
})
public class FieldOrderType {

    @XmlElementRef(name = "Path", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class)
    protected JAXBElement<? extends BasePathToElementType> path;
    @XmlAttribute(name = "Order", required = true)
    protected SortDirectionType order;

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PathToUnindexedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToExtendedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BasePathToElementType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToIndexedFieldType }{@code >}
     *     
     */
    public JAXBElement<? extends BasePathToElementType> getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PathToUnindexedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToExtendedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BasePathToElementType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToIndexedFieldType }{@code >}
     *     
     */
    public void setPath(JAXBElement<? extends BasePathToElementType> value) {
        this.path = ((JAXBElement<? extends BasePathToElementType> ) value);
    }

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
