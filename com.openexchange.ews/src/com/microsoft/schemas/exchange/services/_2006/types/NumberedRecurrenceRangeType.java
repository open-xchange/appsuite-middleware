
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NumberedRecurrenceRangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NumberedRecurrenceRangeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrenceRangeBaseType">
 *       &lt;sequence>
 *         &lt;element name="NumberOfOccurrences" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NumberedRecurrenceRangeType", propOrder = {
    "numberOfOccurrences"
})
public class NumberedRecurrenceRangeType
    extends RecurrenceRangeBaseType
{

    @XmlElement(name = "NumberOfOccurrences")
    protected int numberOfOccurrences;

    /**
     * Gets the value of the numberOfOccurrences property.
     * 
     */
    public int getNumberOfOccurrences() {
        return numberOfOccurrences;
    }

    /**
     * Sets the value of the numberOfOccurrences property.
     * 
     */
    public void setNumberOfOccurrences(int value) {
        this.numberOfOccurrences = value;
    }

}
