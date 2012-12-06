
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfEncryptedSharedFolderDataType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfInvalidRecipientsType;


/**
 * <p>Java class for GetSharingMetadataResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSharingMetadataResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="EncryptedSharedFolderDataCollection" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEncryptedSharedFolderDataType"/>
 *         &lt;element name="InvalidRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfInvalidRecipientsType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSharingMetadataResponseMessageType", propOrder = {
    "encryptedSharedFolderDataCollection",
    "invalidRecipients"
})
public class GetSharingMetadataResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "EncryptedSharedFolderDataCollection")
    protected ArrayOfEncryptedSharedFolderDataType encryptedSharedFolderDataCollection;
    @XmlElement(name = "InvalidRecipients")
    protected ArrayOfInvalidRecipientsType invalidRecipients;

    /**
     * Gets the value of the encryptedSharedFolderDataCollection property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEncryptedSharedFolderDataType }
     *     
     */
    public ArrayOfEncryptedSharedFolderDataType getEncryptedSharedFolderDataCollection() {
        return encryptedSharedFolderDataCollection;
    }

    /**
     * Sets the value of the encryptedSharedFolderDataCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEncryptedSharedFolderDataType }
     *     
     */
    public void setEncryptedSharedFolderDataCollection(ArrayOfEncryptedSharedFolderDataType value) {
        this.encryptedSharedFolderDataCollection = value;
    }

    /**
     * Gets the value of the invalidRecipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInvalidRecipientsType }
     *     
     */
    public ArrayOfInvalidRecipientsType getInvalidRecipients() {
        return invalidRecipients;
    }

    /**
     * Sets the value of the invalidRecipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInvalidRecipientsType }
     *     
     */
    public void setInvalidRecipients(ArrayOfInvalidRecipientsType value) {
        this.invalidRecipients = value;
    }

}
