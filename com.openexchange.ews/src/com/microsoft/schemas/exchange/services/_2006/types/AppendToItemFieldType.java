
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppendToItemFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AppendToItemFieldType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemChangeDescriptionType">
 *       &lt;choice>
 *         &lt;element name="Item" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType"/>
 *         &lt;element name="Message" type="{http://schemas.microsoft.com/exchange/services/2006/types}MessageType"/>
 *         &lt;element name="CalendarItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarItemType"/>
 *         &lt;element name="Contact" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContactItemType"/>
 *         &lt;element name="DistributionList" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistributionListType"/>
 *         &lt;element name="MeetingMessage" type="{http://schemas.microsoft.com/exchange/services/2006/types}MeetingMessageType"/>
 *         &lt;element name="MeetingRequest" type="{http://schemas.microsoft.com/exchange/services/2006/types}MeetingRequestMessageType"/>
 *         &lt;element name="MeetingResponse" type="{http://schemas.microsoft.com/exchange/services/2006/types}MeetingResponseMessageType"/>
 *         &lt;element name="MeetingCancellation" type="{http://schemas.microsoft.com/exchange/services/2006/types}MeetingCancellationMessageType"/>
 *         &lt;element name="Task" type="{http://schemas.microsoft.com/exchange/services/2006/types}TaskType"/>
 *         &lt;element name="PostItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}PostItemType"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppendToItemFieldType", propOrder = {
    "item",
    "message",
    "calendarItem",
    "contact",
    "distributionList",
    "meetingMessage",
    "meetingRequest",
    "meetingResponse",
    "meetingCancellation",
    "task",
    "postItem"
})
public class AppendToItemFieldType
    extends ItemChangeDescriptionType
{

    @XmlElement(name = "Item")
    protected ItemType item;
    @XmlElement(name = "Message")
    protected MessageType message;
    @XmlElement(name = "CalendarItem")
    protected CalendarItemType calendarItem;
    @XmlElement(name = "Contact")
    protected ContactItemType contact;
    @XmlElement(name = "DistributionList")
    protected DistributionListType distributionList;
    @XmlElement(name = "MeetingMessage")
    protected MeetingMessageType meetingMessage;
    @XmlElement(name = "MeetingRequest")
    protected MeetingRequestMessageType meetingRequest;
    @XmlElement(name = "MeetingResponse")
    protected MeetingResponseMessageType meetingResponse;
    @XmlElement(name = "MeetingCancellation")
    protected MeetingCancellationMessageType meetingCancellation;
    @XmlElement(name = "Task")
    protected TaskType task;
    @XmlElement(name = "PostItem")
    protected PostItemType postItem;

    /**
     * Gets the value of the item property.
     * 
     * @return
     *     possible object is
     *     {@link ItemType }
     *     
     */
    public ItemType getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemType }
     *     
     */
    public void setItem(ItemType value) {
        this.item = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link MessageType }
     *     
     */
    public MessageType getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageType }
     *     
     */
    public void setMessage(MessageType value) {
        this.message = value;
    }

    /**
     * Gets the value of the calendarItem property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarItemType }
     *     
     */
    public CalendarItemType getCalendarItem() {
        return calendarItem;
    }

    /**
     * Sets the value of the calendarItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarItemType }
     *     
     */
    public void setCalendarItem(CalendarItemType value) {
        this.calendarItem = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link ContactItemType }
     *     
     */
    public ContactItemType getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactItemType }
     *     
     */
    public void setContact(ContactItemType value) {
        this.contact = value;
    }

    /**
     * Gets the value of the distributionList property.
     * 
     * @return
     *     possible object is
     *     {@link DistributionListType }
     *     
     */
    public DistributionListType getDistributionList() {
        return distributionList;
    }

    /**
     * Sets the value of the distributionList property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistributionListType }
     *     
     */
    public void setDistributionList(DistributionListType value) {
        this.distributionList = value;
    }

    /**
     * Gets the value of the meetingMessage property.
     * 
     * @return
     *     possible object is
     *     {@link MeetingMessageType }
     *     
     */
    public MeetingMessageType getMeetingMessage() {
        return meetingMessage;
    }

    /**
     * Sets the value of the meetingMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeetingMessageType }
     *     
     */
    public void setMeetingMessage(MeetingMessageType value) {
        this.meetingMessage = value;
    }

    /**
     * Gets the value of the meetingRequest property.
     * 
     * @return
     *     possible object is
     *     {@link MeetingRequestMessageType }
     *     
     */
    public MeetingRequestMessageType getMeetingRequest() {
        return meetingRequest;
    }

    /**
     * Sets the value of the meetingRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeetingRequestMessageType }
     *     
     */
    public void setMeetingRequest(MeetingRequestMessageType value) {
        this.meetingRequest = value;
    }

    /**
     * Gets the value of the meetingResponse property.
     * 
     * @return
     *     possible object is
     *     {@link MeetingResponseMessageType }
     *     
     */
    public MeetingResponseMessageType getMeetingResponse() {
        return meetingResponse;
    }

    /**
     * Sets the value of the meetingResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeetingResponseMessageType }
     *     
     */
    public void setMeetingResponse(MeetingResponseMessageType value) {
        this.meetingResponse = value;
    }

    /**
     * Gets the value of the meetingCancellation property.
     * 
     * @return
     *     possible object is
     *     {@link MeetingCancellationMessageType }
     *     
     */
    public MeetingCancellationMessageType getMeetingCancellation() {
        return meetingCancellation;
    }

    /**
     * Sets the value of the meetingCancellation property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeetingCancellationMessageType }
     *     
     */
    public void setMeetingCancellation(MeetingCancellationMessageType value) {
        this.meetingCancellation = value;
    }

    /**
     * Gets the value of the task property.
     * 
     * @return
     *     possible object is
     *     {@link TaskType }
     *     
     */
    public TaskType getTask() {
        return task;
    }

    /**
     * Sets the value of the task property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskType }
     *     
     */
    public void setTask(TaskType value) {
        this.task = value;
    }

    /**
     * Gets the value of the postItem property.
     * 
     * @return
     *     possible object is
     *     {@link PostItemType }
     *     
     */
    public PostItemType getPostItem() {
        return postItem;
    }

    /**
     * Sets the value of the postItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostItemType }
     *     
     */
    public void setPostItem(PostItemType value) {
        this.postItem = value;
    }

}
