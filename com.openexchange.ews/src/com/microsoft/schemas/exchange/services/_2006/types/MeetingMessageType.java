
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for MeetingMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MeetingMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}MessageType">
 *       &lt;sequence>
 *         &lt;element name="AssociatedCalendarItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="IsDelegated" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsOutOfDate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="HasBeenProcessed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ResponseType" type="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseTypeType" minOccurs="0"/>
 *         &lt;element name="UID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RecurrenceId" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="DateTimeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeetingMessageType", propOrder = {
    "associatedCalendarItemId",
    "isDelegated",
    "isOutOfDate",
    "hasBeenProcessed",
    "responseType",
    "uid",
    "recurrenceId",
    "dateTimeStamp"
})
@XmlSeeAlso({
    MeetingCancellationMessageType.class,
    MeetingRequestMessageType.class,
    MeetingResponseMessageType.class
})
public class MeetingMessageType
    extends MessageType
{

    @XmlElement(name = "AssociatedCalendarItemId")
    protected ItemIdType associatedCalendarItemId;
    @XmlElement(name = "IsDelegated")
    protected Boolean isDelegated;
    @XmlElement(name = "IsOutOfDate")
    protected Boolean isOutOfDate;
    @XmlElement(name = "HasBeenProcessed")
    protected Boolean hasBeenProcessed;
    @XmlElement(name = "ResponseType")
    protected ResponseTypeType responseType;
    @XmlElement(name = "UID")
    protected String uid;
    @XmlElement(name = "RecurrenceId")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar recurrenceId;
    @XmlElement(name = "DateTimeStamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeStamp;

    /**
     * Gets the value of the associatedCalendarItemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getAssociatedCalendarItemId() {
        return associatedCalendarItemId;
    }

    /**
     * Sets the value of the associatedCalendarItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setAssociatedCalendarItemId(ItemIdType value) {
        this.associatedCalendarItemId = value;
    }

    /**
     * Gets the value of the isDelegated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsDelegated() {
        return isDelegated;
    }

    /**
     * Sets the value of the isDelegated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsDelegated(Boolean value) {
        this.isDelegated = value;
    }

    /**
     * Gets the value of the isOutOfDate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsOutOfDate() {
        return isOutOfDate;
    }

    /**
     * Sets the value of the isOutOfDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsOutOfDate(Boolean value) {
        this.isOutOfDate = value;
    }

    /**
     * Gets the value of the hasBeenProcessed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHasBeenProcessed() {
        return hasBeenProcessed;
    }

    /**
     * Sets the value of the hasBeenProcessed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasBeenProcessed(Boolean value) {
        this.hasBeenProcessed = value;
    }

    /**
     * Gets the value of the responseType property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseTypeType }
     *     
     */
    public ResponseTypeType getResponseType() {
        return responseType;
    }

    /**
     * Sets the value of the responseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseTypeType }
     *     
     */
    public void setResponseType(ResponseTypeType value) {
        this.responseType = value;
    }

    /**
     * Gets the value of the uid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUID() {
        return uid;
    }

    /**
     * Sets the value of the uid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUID(String value) {
        this.uid = value;
    }

    /**
     * Gets the value of the recurrenceId property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRecurrenceId() {
        return recurrenceId;
    }

    /**
     * Sets the value of the recurrenceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRecurrenceId(XMLGregorianCalendar value) {
        this.recurrenceId = value;
    }

    /**
     * Gets the value of the dateTimeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeStamp() {
        return dateTimeStamp;
    }

    /**
     * Sets the value of the dateTimeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeStamp(XMLGregorianCalendar value) {
        this.dateTimeStamp = value;
    }

}
