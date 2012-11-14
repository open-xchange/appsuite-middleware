
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.CalendarItemCreateOrDeleteOperationType;
import com.microsoft.schemas.exchange.services._2006.types.MessageDispositionType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfAllItemsType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for CreateItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="SavedItemFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="Items" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAllItemsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="MessageDisposition" type="{http://schemas.microsoft.com/exchange/services/2006/types}MessageDispositionType" />
 *       &lt;attribute name="SendMeetingInvitations" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarItemCreateOrDeleteOperationType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateItemType", propOrder = {
    "savedItemFolderId",
    "items"
})
public class CreateItemType
    extends BaseRequestType
{

    @XmlElement(name = "SavedItemFolderId")
    protected TargetFolderIdType savedItemFolderId;
    @XmlElement(name = "Items", required = true)
    protected NonEmptyArrayOfAllItemsType items;
    @XmlAttribute(name = "MessageDisposition")
    protected MessageDispositionType messageDisposition;
    @XmlAttribute(name = "SendMeetingInvitations")
    protected CalendarItemCreateOrDeleteOperationType sendMeetingInvitations;

    /**
     * Gets the value of the savedItemFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getSavedItemFolderId() {
        return savedItemFolderId;
    }

    /**
     * Sets the value of the savedItemFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setSavedItemFolderId(TargetFolderIdType value) {
        this.savedItemFolderId = value;
    }

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public NonEmptyArrayOfAllItemsType getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public void setItems(NonEmptyArrayOfAllItemsType value) {
        this.items = value;
    }

    /**
     * Gets the value of the messageDisposition property.
     * 
     * @return
     *     possible object is
     *     {@link MessageDispositionType }
     *     
     */
    public MessageDispositionType getMessageDisposition() {
        return messageDisposition;
    }

    /**
     * Sets the value of the messageDisposition property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageDispositionType }
     *     
     */
    public void setMessageDisposition(MessageDispositionType value) {
        this.messageDisposition = value;
    }

    /**
     * Gets the value of the sendMeetingInvitations property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarItemCreateOrDeleteOperationType }
     *     
     */
    public CalendarItemCreateOrDeleteOperationType getSendMeetingInvitations() {
        return sendMeetingInvitations;
    }

    /**
     * Sets the value of the sendMeetingInvitations property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarItemCreateOrDeleteOperationType }
     *     
     */
    public void setSendMeetingInvitations(CalendarItemCreateOrDeleteOperationType value) {
        this.sendMeetingInvitations = value;
    }

}
