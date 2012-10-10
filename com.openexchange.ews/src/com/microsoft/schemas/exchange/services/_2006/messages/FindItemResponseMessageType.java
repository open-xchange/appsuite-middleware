
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FindItemParentType;


/**
 * <p>Java class for FindItemResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindItemResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="RootFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}FindItemParentType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindItemResponseMessageType", propOrder = {
    "rootFolder"
})
public class FindItemResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "RootFolder")
    protected FindItemParentType rootFolder;

    /**
     * Gets the value of the rootFolder property.
     * 
     * @return
     *     possible object is
     *     {@link FindItemParentType }
     *     
     */
    public FindItemParentType getRootFolder() {
        return rootFolder;
    }

    /**
     * Sets the value of the rootFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindItemParentType }
     *     
     */
    public void setRootFolder(FindItemParentType value) {
        this.rootFolder = value;
    }

}
