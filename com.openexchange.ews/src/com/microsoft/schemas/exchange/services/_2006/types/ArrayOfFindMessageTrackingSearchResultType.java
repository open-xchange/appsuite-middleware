
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfFindMessageTrackingSearchResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfFindMessageTrackingSearchResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="MessageTrackingSearchResult" type="{http://schemas.microsoft.com/exchange/services/2006/types}FindMessageTrackingSearchResultType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfFindMessageTrackingSearchResultType", propOrder = {
    "messageTrackingSearchResult"
})
public class ArrayOfFindMessageTrackingSearchResultType {

    @XmlElement(name = "MessageTrackingSearchResult")
    protected List<FindMessageTrackingSearchResultType> messageTrackingSearchResult;

    /**
     * Gets the value of the messageTrackingSearchResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageTrackingSearchResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageTrackingSearchResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FindMessageTrackingSearchResultType }
     * 
     * 
     */
    public List<FindMessageTrackingSearchResultType> getMessageTrackingSearchResult() {
        if (messageTrackingSearchResult == null) {
            messageTrackingSearchResult = new ArrayList<FindMessageTrackingSearchResultType>();
        }
        return this.messageTrackingSearchResult;
    }

}
