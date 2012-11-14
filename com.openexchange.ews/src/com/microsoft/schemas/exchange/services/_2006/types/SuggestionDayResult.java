
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SuggestionDayResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SuggestionDayResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="DayQuality" type="{http://schemas.microsoft.com/exchange/services/2006/types}SuggestionQuality"/>
 *         &lt;element name="SuggestionArray" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSuggestion" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuggestionDayResult", propOrder = {
    "date",
    "dayQuality",
    "suggestionArray"
})
public class SuggestionDayResult {

    @XmlElement(name = "Date", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar date;
    @XmlElement(name = "DayQuality", required = true)
    protected SuggestionQuality dayQuality;
    @XmlElement(name = "SuggestionArray")
    protected ArrayOfSuggestion suggestionArray;

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the dayQuality property.
     * 
     * @return
     *     possible object is
     *     {@link SuggestionQuality }
     *     
     */
    public SuggestionQuality getDayQuality() {
        return dayQuality;
    }

    /**
     * Sets the value of the dayQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuggestionQuality }
     *     
     */
    public void setDayQuality(SuggestionQuality value) {
        this.dayQuality = value;
    }

    /**
     * Gets the value of the suggestionArray property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSuggestion }
     *     
     */
    public ArrayOfSuggestion getSuggestionArray() {
        return suggestionArray;
    }

    /**
     * Sets the value of the suggestionArray property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSuggestion }
     *     
     */
    public void setSuggestionArray(ArrayOfSuggestion value) {
        this.suggestionArray = value;
    }

}
