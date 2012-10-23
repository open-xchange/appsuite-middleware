
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AttachmentResponseShapeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttachmentResponseShapeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IncludeMimeContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="BodyType" type="{http://schemas.microsoft.com/exchange/services/2006/types}BodyTypeResponseType" minOccurs="0"/>
 *         &lt;element name="FilterHtmlContent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="AdditionalProperties" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfPathsToElementType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentResponseShapeType", propOrder = {
    "includeMimeContent",
    "bodyType",
    "filterHtmlContent",
    "additionalProperties"
})
public class AttachmentResponseShapeType {

    @XmlElement(name = "IncludeMimeContent")
    protected Boolean includeMimeContent;
    @XmlElement(name = "BodyType")
    protected BodyTypeResponseType bodyType;
    @XmlElement(name = "FilterHtmlContent")
    protected Boolean filterHtmlContent;
    @XmlElement(name = "AdditionalProperties")
    protected NonEmptyArrayOfPathsToElementType additionalProperties;

    /**
     * Gets the value of the includeMimeContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeMimeContent() {
        return includeMimeContent;
    }

    /**
     * Sets the value of the includeMimeContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeMimeContent(Boolean value) {
        this.includeMimeContent = value;
    }

    /**
     * Gets the value of the bodyType property.
     * 
     * @return
     *     possible object is
     *     {@link BodyTypeResponseType }
     *     
     */
    public BodyTypeResponseType getBodyType() {
        return bodyType;
    }

    /**
     * Sets the value of the bodyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyTypeResponseType }
     *     
     */
    public void setBodyType(BodyTypeResponseType value) {
        this.bodyType = value;
    }

    /**
     * Gets the value of the filterHtmlContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFilterHtmlContent() {
        return filterHtmlContent;
    }

    /**
     * Sets the value of the filterHtmlContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFilterHtmlContent(Boolean value) {
        this.filterHtmlContent = value;
    }

    /**
     * Gets the value of the additionalProperties property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfPathsToElementType }
     *     
     */
    public NonEmptyArrayOfPathsToElementType getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Sets the value of the additionalProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfPathsToElementType }
     *     
     */
    public void setAdditionalProperties(NonEmptyArrayOfPathsToElementType value) {
        this.additionalProperties = value;
    }

}
