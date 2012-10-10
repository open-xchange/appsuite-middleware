
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Suggestion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Suggestion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MeetingTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="IsWorkTime" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="SuggestionQuality" type="{http://schemas.microsoft.com/exchange/services/2006/types}SuggestionQuality"/>
 *         &lt;element name="AttendeeConflictDataArray" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfAttendeeConflictData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Suggestion", propOrder = {
    "meetingTime",
    "isWorkTime",
    "suggestionQuality",
    "attendeeConflictDataArray"
})
public class Suggestion {

    @XmlElement(name = "MeetingTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar meetingTime;
    @XmlElement(name = "IsWorkTime")
    protected boolean isWorkTime;
    @XmlElement(name = "SuggestionQuality", required = true)
    protected SuggestionQuality suggestionQuality;
    @XmlElement(name = "AttendeeConflictDataArray")
    protected ArrayOfAttendeeConflictData attendeeConflictDataArray;

    /**
     * Gets the value of the meetingTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMeetingTime() {
        return meetingTime;
    }

    /**
     * Sets the value of the meetingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMeetingTime(XMLGregorianCalendar value) {
        this.meetingTime = value;
    }

    /**
     * Gets the value of the isWorkTime property.
     * 
     */
    public boolean isIsWorkTime() {
        return isWorkTime;
    }

    /**
     * Sets the value of the isWorkTime property.
     * 
     */
    public void setIsWorkTime(boolean value) {
        this.isWorkTime = value;
    }

    /**
     * Gets the value of the suggestionQuality property.
     * 
     * @return
     *     possible object is
     *     {@link SuggestionQuality }
     *     
     */
    public SuggestionQuality getSuggestionQuality() {
        return suggestionQuality;
    }

    /**
     * Sets the value of the suggestionQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuggestionQuality }
     *     
     */
    public void setSuggestionQuality(SuggestionQuality value) {
        this.suggestionQuality = value;
    }

    /**
     * Gets the value of the attendeeConflictDataArray property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAttendeeConflictData }
     *     
     */
    public ArrayOfAttendeeConflictData getAttendeeConflictDataArray() {
        return attendeeConflictDataArray;
    }

    /**
     * Sets the value of the attendeeConflictDataArray property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAttendeeConflictData }
     *     
     */
    public void setAttendeeConflictDataArray(ArrayOfAttendeeConflictData value) {
        this.attendeeConflictDataArray = value;
    }

}
