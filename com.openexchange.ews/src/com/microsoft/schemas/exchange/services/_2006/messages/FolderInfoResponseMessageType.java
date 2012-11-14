
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfFoldersType;


/**
 * <p>Java class for FolderInfoResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FolderInfoResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="Folders" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfFoldersType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FolderInfoResponseMessageType", propOrder = {
    "folders"
})
public class FolderInfoResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "Folders")
    protected ArrayOfFoldersType folders;

    /**
     * Gets the value of the folders property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFoldersType }
     *     
     */
    public ArrayOfFoldersType getFolders() {
        return folders;
    }

    /**
     * Sets the value of the folders property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFoldersType }
     *     
     */
    public void setFolders(ArrayOfFoldersType value) {
        this.folders = value;
    }

}
