
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfSmtpAddressType;
import com.microsoft.schemas.exchange.services._2006.types.FolderIdType;


/**
 * <p>Java class for GetSharingMetadataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSharingMetadataType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="IdOfFolderToShare" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderIdType"/>
 *         &lt;element name="SenderSmtpAddress" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *         &lt;element name="Recipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSmtpAddressType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSharingMetadataType", propOrder = {
    "idOfFolderToShare",
    "senderSmtpAddress",
    "recipients"
})
public class GetSharingMetadataType
    extends BaseRequestType
{

    @XmlElement(name = "IdOfFolderToShare", required = true)
    protected FolderIdType idOfFolderToShare;
    @XmlElement(name = "SenderSmtpAddress", required = true)
    protected String senderSmtpAddress;
    @XmlElement(name = "Recipients", required = true)
    protected ArrayOfSmtpAddressType recipients;

    /**
     * Gets the value of the idOfFolderToShare property.
     * 
     * @return
     *     possible object is
     *     {@link FolderIdType }
     *     
     */
    public FolderIdType getIdOfFolderToShare() {
        return idOfFolderToShare;
    }

    /**
     * Sets the value of the idOfFolderToShare property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderIdType }
     *     
     */
    public void setIdOfFolderToShare(FolderIdType value) {
        this.idOfFolderToShare = value;
    }

    /**
     * Gets the value of the senderSmtpAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderSmtpAddress() {
        return senderSmtpAddress;
    }

    /**
     * Sets the value of the senderSmtpAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderSmtpAddress(String value) {
        this.senderSmtpAddress = value;
    }

    /**
     * Gets the value of the recipients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public ArrayOfSmtpAddressType getRecipients() {
        return recipients;
    }

    /**
     * Sets the value of the recipients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSmtpAddressType }
     *     
     */
    public void setRecipients(ArrayOfSmtpAddressType value) {
        this.recipients = value;
    }

}
