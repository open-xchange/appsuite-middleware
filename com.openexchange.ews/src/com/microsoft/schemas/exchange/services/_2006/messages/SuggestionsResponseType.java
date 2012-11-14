
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfSuggestionDayResult;


/**
 * <p>Java class for SuggestionsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SuggestionsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseMessage" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType" minOccurs="0"/>
 *         &lt;element name="SuggestionDayResultArray" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfSuggestionDayResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuggestionsResponseType", propOrder = {
    "responseMessage",
    "suggestionDayResultArray"
})
public class SuggestionsResponseType {

    @XmlElement(name = "ResponseMessage")
    protected ResponseMessageType responseMessage;
    @XmlElement(name = "SuggestionDayResultArray")
    protected ArrayOfSuggestionDayResult suggestionDayResultArray;

    /**
     * Gets the value of the responseMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseMessageType }
     *     
     */
    public ResponseMessageType getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the value of the responseMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseMessageType }
     *     
     */
    public void setResponseMessage(ResponseMessageType value) {
        this.responseMessage = value;
    }

    /**
     * Gets the value of the suggestionDayResultArray property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSuggestionDayResult }
     *     
     */
    public ArrayOfSuggestionDayResult getSuggestionDayResultArray() {
        return suggestionDayResultArray;
    }

    /**
     * Sets the value of the suggestionDayResultArray property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSuggestionDayResult }
     *     
     */
    public void setSuggestionDayResultArray(ArrayOfSuggestionDayResult value) {
        this.suggestionDayResultArray = value;
    }

}
