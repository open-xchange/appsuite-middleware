
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Internal abstract base type for reply objects.
 *         Should not appear in client code
 *       
 * 
 * <p>Java class for ResponseObjectCoreType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseObjectCoreType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}MessageType">
 *       &lt;sequence>
 *         &lt;element name="ReferenceItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseObjectCoreType", propOrder = {
    "referenceItemId"
})
@XmlSeeAlso({
    ResponseObjectType.class
})
public abstract class ResponseObjectCoreType
    extends MessageType
{

    @XmlElement(name = "ReferenceItemId")
    protected ItemIdType referenceItemId;

    /**
     * Gets the value of the referenceItemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getReferenceItemId() {
        return referenceItemId;
    }

    /**
     * Sets the value of the referenceItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setReferenceItemId(ItemIdType value) {
        this.referenceItemId = value;
    }

}
