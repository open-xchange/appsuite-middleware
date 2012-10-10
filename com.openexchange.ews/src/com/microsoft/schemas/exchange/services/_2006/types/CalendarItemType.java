
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CalendarItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="UID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RecurrenceId" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="DateTimeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="End" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="OriginalStart" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="IsAllDayEvent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="LegacyFreeBusyStatus" type="{http://schemas.microsoft.com/exchange/services/2006/types}LegacyFreeBusyType" minOccurs="0"/>
 *         &lt;element name="Location" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="When" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsMeeting" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsCancelled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsRecurring" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="MeetingRequestWasSent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsResponseRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CalendarItemType" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarItemTypeType" minOccurs="0"/>
 *         &lt;element name="MyResponseType" type="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseTypeType" minOccurs="0"/>
 *         &lt;element name="Organizer" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="RequiredAttendees" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttendeesType" minOccurs="0"/>
 *         &lt;element name="OptionalAttendees" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttendeesType" minOccurs="0"/>
 *         &lt;element name="Resources" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttendeesType" minOccurs="0"/>
 *         &lt;element name="ConflictingMeetingCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="AdjacentMeetingCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ConflictingMeetings" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAllItemsType" minOccurs="0"/>
 *         &lt;element name="AdjacentMeetings" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAllItemsType" minOccurs="0"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TimeZone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AppointmentReplyTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="AppointmentSequenceNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="AppointmentState" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Recurrence" type="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrenceType" minOccurs="0"/>
 *         &lt;element name="FirstOccurrence" type="{http://schemas.microsoft.com/exchange/services/2006/types}OccurrenceInfoType" minOccurs="0"/>
 *         &lt;element name="LastOccurrence" type="{http://schemas.microsoft.com/exchange/services/2006/types}OccurrenceInfoType" minOccurs="0"/>
 *         &lt;element name="ModifiedOccurrences" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfOccurrenceInfoType" minOccurs="0"/>
 *         &lt;element name="DeletedOccurrences" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfDeletedOccurrencesType" minOccurs="0"/>
 *         &lt;element name="MeetingTimeZone" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeZoneType" minOccurs="0"/>
 *         &lt;element name="StartTimeZone" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeZoneDefinitionType" minOccurs="0"/>
 *         &lt;element name="EndTimeZone" type="{http://schemas.microsoft.com/exchange/services/2006/types}TimeZoneDefinitionType" minOccurs="0"/>
 *         &lt;element name="ConferenceType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="AllowNewTimeProposal" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsOnlineMeeting" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="MeetingWorkspaceUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NetShowUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarItemType", propOrder = {
    "uid",
    "recurrenceId",
    "dateTimeStamp",
    "start",
    "end",
    "originalStart",
    "isAllDayEvent",
    "legacyFreeBusyStatus",
    "location",
    "when",
    "isMeeting",
    "isCancelled",
    "isRecurring",
    "meetingRequestWasSent",
    "isResponseRequested",
    "calendarItemType",
    "myResponseType",
    "organizer",
    "requiredAttendees",
    "optionalAttendees",
    "resources",
    "conflictingMeetingCount",
    "adjacentMeetingCount",
    "conflictingMeetings",
    "adjacentMeetings",
    "duration",
    "timeZone",
    "appointmentReplyTime",
    "appointmentSequenceNumber",
    "appointmentState",
    "recurrence",
    "firstOccurrence",
    "lastOccurrence",
    "modifiedOccurrences",
    "deletedOccurrences",
    "meetingTimeZone",
    "startTimeZone",
    "endTimeZone",
    "conferenceType",
    "allowNewTimeProposal",
    "isOnlineMeeting",
    "meetingWorkspaceUrl",
    "netShowUrl"
})
public class CalendarItemType
    extends ItemType
{

    @XmlElement(name = "UID")
    protected String uid;
    @XmlElement(name = "RecurrenceId")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar recurrenceId;
    @XmlElement(name = "DateTimeStamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeStamp;
    @XmlElement(name = "Start")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar start;
    @XmlElement(name = "End")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar end;
    @XmlElement(name = "OriginalStart")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar originalStart;
    @XmlElement(name = "IsAllDayEvent")
    protected Boolean isAllDayEvent;
    @XmlElement(name = "LegacyFreeBusyStatus")
    protected LegacyFreeBusyType legacyFreeBusyStatus;
    @XmlElement(name = "Location")
    protected String location;
    @XmlElement(name = "When")
    protected String when;
    @XmlElement(name = "IsMeeting")
    protected Boolean isMeeting;
    @XmlElement(name = "IsCancelled")
    protected Boolean isCancelled;
    @XmlElement(name = "IsRecurring")
    protected Boolean isRecurring;
    @XmlElement(name = "MeetingRequestWasSent")
    protected Boolean meetingRequestWasSent;
    @XmlElement(name = "IsResponseRequested")
    protected Boolean isResponseRequested;
    @XmlElement(name = "CalendarItemType")
    protected CalendarItemTypeType calendarItemType;
    @XmlElement(name = "MyResponseType")
    protected ResponseTypeType myResponseType;
    @XmlElement(name = "Organizer")
    protected SingleRecipientType organizer;
    @XmlElement(name = "RequiredAttendees")
    protected NonEmptyArrayOfAttendeesType requiredAttendees;
    @XmlElement(name = "OptionalAttendees")
    protected NonEmptyArrayOfAttendeesType optionalAttendees;
    @XmlElement(name = "Resources")
    protected NonEmptyArrayOfAttendeesType resources;
    @XmlElement(name = "ConflictingMeetingCount")
    protected Integer conflictingMeetingCount;
    @XmlElement(name = "AdjacentMeetingCount")
    protected Integer adjacentMeetingCount;
    @XmlElement(name = "ConflictingMeetings")
    protected NonEmptyArrayOfAllItemsType conflictingMeetings;
    @XmlElement(name = "AdjacentMeetings")
    protected NonEmptyArrayOfAllItemsType adjacentMeetings;
    @XmlElement(name = "Duration")
    protected String duration;
    @XmlElement(name = "TimeZone")
    protected String timeZone;
    @XmlElement(name = "AppointmentReplyTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar appointmentReplyTime;
    @XmlElement(name = "AppointmentSequenceNumber")
    protected Integer appointmentSequenceNumber;
    @XmlElement(name = "AppointmentState")
    protected Integer appointmentState;
    @XmlElement(name = "Recurrence")
    protected RecurrenceType recurrence;
    @XmlElement(name = "FirstOccurrence")
    protected OccurrenceInfoType firstOccurrence;
    @XmlElement(name = "LastOccurrence")
    protected OccurrenceInfoType lastOccurrence;
    @XmlElement(name = "ModifiedOccurrences")
    protected NonEmptyArrayOfOccurrenceInfoType modifiedOccurrences;
    @XmlElement(name = "DeletedOccurrences")
    protected NonEmptyArrayOfDeletedOccurrencesType deletedOccurrences;
    @XmlElement(name = "MeetingTimeZone")
    protected TimeZoneType meetingTimeZone;
    @XmlElement(name = "StartTimeZone")
    protected TimeZoneDefinitionType startTimeZone;
    @XmlElement(name = "EndTimeZone")
    protected TimeZoneDefinitionType endTimeZone;
    @XmlElement(name = "ConferenceType")
    protected Integer conferenceType;
    @XmlElement(name = "AllowNewTimeProposal")
    protected Boolean allowNewTimeProposal;
    @XmlElement(name = "IsOnlineMeeting")
    protected Boolean isOnlineMeeting;
    @XmlElement(name = "MeetingWorkspaceUrl")
    protected String meetingWorkspaceUrl;
    @XmlElement(name = "NetShowUrl")
    protected String netShowUrl;

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

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStart(XMLGregorianCalendar value) {
        this.start = value;
    }

    /**
     * Gets the value of the end property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEnd(XMLGregorianCalendar value) {
        this.end = value;
    }

    /**
     * Gets the value of the originalStart property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOriginalStart() {
        return originalStart;
    }

    /**
     * Sets the value of the originalStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOriginalStart(XMLGregorianCalendar value) {
        this.originalStart = value;
    }

    /**
     * Gets the value of the isAllDayEvent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAllDayEvent() {
        return isAllDayEvent;
    }

    /**
     * Sets the value of the isAllDayEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAllDayEvent(Boolean value) {
        this.isAllDayEvent = value;
    }

    /**
     * Gets the value of the legacyFreeBusyStatus property.
     * 
     * @return
     *     possible object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public LegacyFreeBusyType getLegacyFreeBusyStatus() {
        return legacyFreeBusyStatus;
    }

    /**
     * Sets the value of the legacyFreeBusyStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public void setLegacyFreeBusyStatus(LegacyFreeBusyType value) {
        this.legacyFreeBusyStatus = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the when property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWhen() {
        return when;
    }

    /**
     * Sets the value of the when property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWhen(String value) {
        this.when = value;
    }

    /**
     * Gets the value of the isMeeting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMeeting() {
        return isMeeting;
    }

    /**
     * Sets the value of the isMeeting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMeeting(Boolean value) {
        this.isMeeting = value;
    }

    /**
     * Gets the value of the isCancelled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsCancelled() {
        return isCancelled;
    }

    /**
     * Sets the value of the isCancelled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsCancelled(Boolean value) {
        this.isCancelled = value;
    }

    /**
     * Gets the value of the isRecurring property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRecurring() {
        return isRecurring;
    }

    /**
     * Sets the value of the isRecurring property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRecurring(Boolean value) {
        this.isRecurring = value;
    }

    /**
     * Gets the value of the meetingRequestWasSent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMeetingRequestWasSent() {
        return meetingRequestWasSent;
    }

    /**
     * Sets the value of the meetingRequestWasSent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeetingRequestWasSent(Boolean value) {
        this.meetingRequestWasSent = value;
    }

    /**
     * Gets the value of the isResponseRequested property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsResponseRequested() {
        return isResponseRequested;
    }

    /**
     * Sets the value of the isResponseRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsResponseRequested(Boolean value) {
        this.isResponseRequested = value;
    }

    /**
     * Gets the value of the calendarItemType property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarItemTypeType }
     *     
     */
    public CalendarItemTypeType getCalendarItemType() {
        return calendarItemType;
    }

    /**
     * Sets the value of the calendarItemType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarItemTypeType }
     *     
     */
    public void setCalendarItemType(CalendarItemTypeType value) {
        this.calendarItemType = value;
    }

    /**
     * Gets the value of the myResponseType property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseTypeType }
     *     
     */
    public ResponseTypeType getMyResponseType() {
        return myResponseType;
    }

    /**
     * Sets the value of the myResponseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseTypeType }
     *     
     */
    public void setMyResponseType(ResponseTypeType value) {
        this.myResponseType = value;
    }

    /**
     * Gets the value of the organizer property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getOrganizer() {
        return organizer;
    }

    /**
     * Sets the value of the organizer property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setOrganizer(SingleRecipientType value) {
        this.organizer = value;
    }

    /**
     * Gets the value of the requiredAttendees property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public NonEmptyArrayOfAttendeesType getRequiredAttendees() {
        return requiredAttendees;
    }

    /**
     * Sets the value of the requiredAttendees property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public void setRequiredAttendees(NonEmptyArrayOfAttendeesType value) {
        this.requiredAttendees = value;
    }

    /**
     * Gets the value of the optionalAttendees property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public NonEmptyArrayOfAttendeesType getOptionalAttendees() {
        return optionalAttendees;
    }

    /**
     * Sets the value of the optionalAttendees property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public void setOptionalAttendees(NonEmptyArrayOfAttendeesType value) {
        this.optionalAttendees = value;
    }

    /**
     * Gets the value of the resources property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public NonEmptyArrayOfAttendeesType getResources() {
        return resources;
    }

    /**
     * Sets the value of the resources property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAttendeesType }
     *     
     */
    public void setResources(NonEmptyArrayOfAttendeesType value) {
        this.resources = value;
    }

    /**
     * Gets the value of the conflictingMeetingCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getConflictingMeetingCount() {
        return conflictingMeetingCount;
    }

    /**
     * Sets the value of the conflictingMeetingCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setConflictingMeetingCount(Integer value) {
        this.conflictingMeetingCount = value;
    }

    /**
     * Gets the value of the adjacentMeetingCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAdjacentMeetingCount() {
        return adjacentMeetingCount;
    }

    /**
     * Sets the value of the adjacentMeetingCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAdjacentMeetingCount(Integer value) {
        this.adjacentMeetingCount = value;
    }

    /**
     * Gets the value of the conflictingMeetings property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public NonEmptyArrayOfAllItemsType getConflictingMeetings() {
        return conflictingMeetings;
    }

    /**
     * Sets the value of the conflictingMeetings property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public void setConflictingMeetings(NonEmptyArrayOfAllItemsType value) {
        this.conflictingMeetings = value;
    }

    /**
     * Gets the value of the adjacentMeetings property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public NonEmptyArrayOfAllItemsType getAdjacentMeetings() {
        return adjacentMeetings;
    }

    /**
     * Sets the value of the adjacentMeetings property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAllItemsType }
     *     
     */
    public void setAdjacentMeetings(NonEmptyArrayOfAllItemsType value) {
        this.adjacentMeetings = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDuration(String value) {
        this.duration = value;
    }

    /**
     * Gets the value of the timeZone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the value of the timeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeZone(String value) {
        this.timeZone = value;
    }

    /**
     * Gets the value of the appointmentReplyTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAppointmentReplyTime() {
        return appointmentReplyTime;
    }

    /**
     * Sets the value of the appointmentReplyTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAppointmentReplyTime(XMLGregorianCalendar value) {
        this.appointmentReplyTime = value;
    }

    /**
     * Gets the value of the appointmentSequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAppointmentSequenceNumber() {
        return appointmentSequenceNumber;
    }

    /**
     * Sets the value of the appointmentSequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAppointmentSequenceNumber(Integer value) {
        this.appointmentSequenceNumber = value;
    }

    /**
     * Gets the value of the appointmentState property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAppointmentState() {
        return appointmentState;
    }

    /**
     * Sets the value of the appointmentState property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAppointmentState(Integer value) {
        this.appointmentState = value;
    }

    /**
     * Gets the value of the recurrence property.
     * 
     * @return
     *     possible object is
     *     {@link RecurrenceType }
     *     
     */
    public RecurrenceType getRecurrence() {
        return recurrence;
    }

    /**
     * Sets the value of the recurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecurrenceType }
     *     
     */
    public void setRecurrence(RecurrenceType value) {
        this.recurrence = value;
    }

    /**
     * Gets the value of the firstOccurrence property.
     * 
     * @return
     *     possible object is
     *     {@link OccurrenceInfoType }
     *     
     */
    public OccurrenceInfoType getFirstOccurrence() {
        return firstOccurrence;
    }

    /**
     * Sets the value of the firstOccurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link OccurrenceInfoType }
     *     
     */
    public void setFirstOccurrence(OccurrenceInfoType value) {
        this.firstOccurrence = value;
    }

    /**
     * Gets the value of the lastOccurrence property.
     * 
     * @return
     *     possible object is
     *     {@link OccurrenceInfoType }
     *     
     */
    public OccurrenceInfoType getLastOccurrence() {
        return lastOccurrence;
    }

    /**
     * Sets the value of the lastOccurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link OccurrenceInfoType }
     *     
     */
    public void setLastOccurrence(OccurrenceInfoType value) {
        this.lastOccurrence = value;
    }

    /**
     * Gets the value of the modifiedOccurrences property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfOccurrenceInfoType }
     *     
     */
    public NonEmptyArrayOfOccurrenceInfoType getModifiedOccurrences() {
        return modifiedOccurrences;
    }

    /**
     * Sets the value of the modifiedOccurrences property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfOccurrenceInfoType }
     *     
     */
    public void setModifiedOccurrences(NonEmptyArrayOfOccurrenceInfoType value) {
        this.modifiedOccurrences = value;
    }

    /**
     * Gets the value of the deletedOccurrences property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfDeletedOccurrencesType }
     *     
     */
    public NonEmptyArrayOfDeletedOccurrencesType getDeletedOccurrences() {
        return deletedOccurrences;
    }

    /**
     * Sets the value of the deletedOccurrences property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfDeletedOccurrencesType }
     *     
     */
    public void setDeletedOccurrences(NonEmptyArrayOfDeletedOccurrencesType value) {
        this.deletedOccurrences = value;
    }

    /**
     * Gets the value of the meetingTimeZone property.
     * 
     * @return
     *     possible object is
     *     {@link TimeZoneType }
     *     
     */
    public TimeZoneType getMeetingTimeZone() {
        return meetingTimeZone;
    }

    /**
     * Sets the value of the meetingTimeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeZoneType }
     *     
     */
    public void setMeetingTimeZone(TimeZoneType value) {
        this.meetingTimeZone = value;
    }

    /**
     * Gets the value of the startTimeZone property.
     * 
     * @return
     *     possible object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public TimeZoneDefinitionType getStartTimeZone() {
        return startTimeZone;
    }

    /**
     * Sets the value of the startTimeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public void setStartTimeZone(TimeZoneDefinitionType value) {
        this.startTimeZone = value;
    }

    /**
     * Gets the value of the endTimeZone property.
     * 
     * @return
     *     possible object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public TimeZoneDefinitionType getEndTimeZone() {
        return endTimeZone;
    }

    /**
     * Sets the value of the endTimeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeZoneDefinitionType }
     *     
     */
    public void setEndTimeZone(TimeZoneDefinitionType value) {
        this.endTimeZone = value;
    }

    /**
     * Gets the value of the conferenceType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getConferenceType() {
        return conferenceType;
    }

    /**
     * Sets the value of the conferenceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setConferenceType(Integer value) {
        this.conferenceType = value;
    }

    /**
     * Gets the value of the allowNewTimeProposal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAllowNewTimeProposal() {
        return allowNewTimeProposal;
    }

    /**
     * Sets the value of the allowNewTimeProposal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowNewTimeProposal(Boolean value) {
        this.allowNewTimeProposal = value;
    }

    /**
     * Gets the value of the isOnlineMeeting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsOnlineMeeting() {
        return isOnlineMeeting;
    }

    /**
     * Sets the value of the isOnlineMeeting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsOnlineMeeting(Boolean value) {
        this.isOnlineMeeting = value;
    }

    /**
     * Gets the value of the meetingWorkspaceUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMeetingWorkspaceUrl() {
        return meetingWorkspaceUrl;
    }

    /**
     * Sets the value of the meetingWorkspaceUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMeetingWorkspaceUrl(String value) {
        this.meetingWorkspaceUrl = value;
    }

    /**
     * Gets the value of the netShowUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetShowUrl() {
        return netShowUrl;
    }

    /**
     * Sets the value of the netShowUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetShowUrl(String value) {
        this.netShowUrl = value;
    }

}
