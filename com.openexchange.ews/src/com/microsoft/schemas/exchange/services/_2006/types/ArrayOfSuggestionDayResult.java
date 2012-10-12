
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfSuggestionDayResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSuggestionDayResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SuggestionDayResult" type="{http://schemas.microsoft.com/exchange/services/2006/types}SuggestionDayResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSuggestionDayResult", propOrder = {
    "suggestionDayResult"
})
public class ArrayOfSuggestionDayResult {

    @XmlElement(name = "SuggestionDayResult")
    protected List<SuggestionDayResult> suggestionDayResult;

    /**
     * Gets the value of the suggestionDayResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the suggestionDayResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuggestionDayResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SuggestionDayResult }
     * 
     * 
     */
    public List<SuggestionDayResult> getSuggestionDayResult() {
        if (suggestionDayResult == null) {
            suggestionDayResult = new ArrayList<SuggestionDayResult>();
        }
        return this.suggestionDayResult;
    }

}
