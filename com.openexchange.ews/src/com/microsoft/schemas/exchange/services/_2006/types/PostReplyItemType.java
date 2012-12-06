
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PostReplyItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostReplyItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}PostReplyItemBaseType">
 *       &lt;sequence>
 *         &lt;element name="NewBodyContent" type="{http://schemas.microsoft.com/exchange/services/2006/types}BodyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostReplyItemType", propOrder = {
    "newBodyContent"
})
public class PostReplyItemType
    extends PostReplyItemBaseType
{

    @XmlElement(name = "NewBodyContent")
    protected BodyType newBodyContent;

    /**
     * Gets the value of the newBodyContent property.
     * 
     * @return
     *     possible object is
     *     {@link BodyType }
     *     
     */
    public BodyType getNewBodyContent() {
        return newBodyContent;
    }

    /**
     * Sets the value of the newBodyContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyType }
     *     
     */
    public void setNewBodyContent(BodyType value) {
        this.newBodyContent = value;
    }

}
