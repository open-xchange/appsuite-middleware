
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FolderIdType;


/**
 * <p>Java class for GetSharingFolderResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSharingFolderResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="SharingFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSharingFolderResponseMessageType", propOrder = {
    "sharingFolderId"
})
public class GetSharingFolderResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "SharingFolderId")
    protected FolderIdType sharingFolderId;

    /**
     * Gets the value of the sharingFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getSharingFolderId() {
        return sharingFolderId;
    }

    /**
     * Sets the value of the sharingFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setSharingFolderId(FolderIdType value) {
        this.sharingFolderId = value;
    }

}
