
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SuggestionsViewOptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SuggestionsViewOptionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GoodThreshold" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MaximumResultsByDay" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MaximumNonWorkHourResultsByDay" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MeetingDurationInMinutes" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MinimumSuggestionQuality" type="{http://schemas.microsoft.com/exchange/services/2006/types}SuggestionQuality" minOccurs="0"/>
 *         &lt;element name="DetailedSuggestionsWindow" type="{http://schemas.microsoft.com/exchange/services/2006/types}Duration"/>
 *         &lt;element name="CurrentMeetingTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="GlobalObjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuggestionsViewOptionsType", propOrder = {
    "goodThreshold",
    "maximumResultsByDay",
    "maximumNonWorkHourResultsByDay",
    "meetingDurationInMinutes",
    "minimumSuggestionQuality",
    "detailedSuggestionsWindow",
    "currentMeetingTime",
    "globalObjectId"
})
public class SuggestionsViewOptionsType {

    @XmlElement(name = "GoodThreshold")
    protected Integer goodThreshold;
    @XmlElement(name = "MaximumResultsByDay")
    protected Integer maximumResultsByDay;
    @XmlElement(name = "MaximumNonWorkHourResultsByDay")
    protected Integer maximumNonWorkHourResultsByDay;
    @XmlElement(name = "MeetingDurationInMinutes")
    protected Integer meetingDurationInMinutes;
    @XmlElement(name = "MinimumSuggestionQuality")
    protected SuggestionQuality minimumSuggestionQuality;
    @XmlElement(name = "DetailedSuggestionsWindow", required = true)
    protected Duration detailedSuggestionsWindow;
    @XmlElement(name = "CurrentMeetingTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar currentMeetingTime;
    @XmlElement(name = "GlobalObjectId")
    protected String globalObjectId;

    /**
     * Gets the value of the goodThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGoodThreshold() {
        return goodThreshold;
    }

    /**
     * Sets the value of the goodThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGoodThreshold(Integer value) {
        this.goodThreshold = value;
    }

    /**
     * Gets the value of the maximumResultsByDay property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaximumResultsByDay() {
        return maximumResultsByDay;
    }

    /**
     * Sets the value of the maximumResultsByDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaximumResultsByDay(Integer value) {
        this.maximumResultsByDay = value;
    }

    /**
     * Gets the value of the maximumNonWorkHourResultsByDay property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaximumNonWorkHourResultsByDay() {
        return maximumNonWorkHourResultsByDay;
    }

    /**
     * Sets the value of the maximumNonWorkHourResultsByDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaximumNonWorkHourResultsByDay(Integer value) {
        this.maximumNonWorkHourResultsByDay = value;
    }

    /**
     * Gets the value of the meetingDurationInMinutes property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMeetingDurationInMinutes() {
        return meetingDurationInMinutes;
    }

    /**
     * Sets the value of the meetingDurationInMinutes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMeetingDurationInMinutes(Integer value) {
        this.meetingDurationInMinutes = value;
    }

    /**
     * Gets the value of the minimumSuggestionQuality property.
     * 
     * @return
     *     possible object is
     *     {@link SuggestionQuality }
     *     
     */
    public SuggestionQuality getMinimumSuggestionQuality() {
        return minimumSuggestionQuality;
    }

    /**
     * Sets the value of the minimumSuggestionQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuggestionQuality }
     *     
     */
    public void setMinimumSuggestionQuality(SuggestionQuality value) {
        this.minimumSuggestionQuality = value;
    }

    /**
     * Gets the value of the detailedSuggestionsWindow property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getDetailedSuggestionsWindow() {
        return detailedSuggestionsWindow;
    }

    /**
     * Sets the value of the detailedSuggestionsWindow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setDetailedSuggestionsWindow(Duration value) {
        this.detailedSuggestionsWindow = value;
    }

    /**
     * Gets the value of the currentMeetingTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCurrentMeetingTime() {
        return currentMeetingTime;
    }

    /**
     * Sets the value of the currentMeetingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCurrentMeetingTime(XMLGregorianCalendar value) {
        this.currentMeetingTime = value;
    }

    /**
     * Gets the value of the globalObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlobalObjectId() {
        return globalObjectId;
    }

    /**
     * Sets the value of the globalObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalObjectId(String value) {
        this.globalObjectId = value;
    }

}
