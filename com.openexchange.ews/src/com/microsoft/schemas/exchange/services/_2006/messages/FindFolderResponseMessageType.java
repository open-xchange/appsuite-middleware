
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FindFolderParentType;


/**
 * <p>Java class for FindFolderResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindFolderResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="RootFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}FindFolderParentType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindFolderResponseMessageType", propOrder = {
    "rootFolder"
})
public class FindFolderResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "RootFolder")
    protected FindFolderParentType rootFolder;

    /**
     * Gets the value of the rootFolder property.
     * 
     * @return
     *     possible object is
     *     {@link FindFolderParentType }
     *     
     */
    public FindFolderParentType getRootFolder() {
        return rootFolder;
    }

    /**
     * Sets the value of the rootFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindFolderParentType }
     *     
     */
    public void setRootFolder(FindFolderParentType value) {
        this.rootFolder = value;
    }

}
