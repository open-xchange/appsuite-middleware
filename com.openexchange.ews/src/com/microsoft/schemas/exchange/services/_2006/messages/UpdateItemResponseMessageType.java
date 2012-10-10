
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ConflictResultsType;


/**
 * <p>Java class for UpdateItemResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateItemResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ItemInfoResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="ConflictResults" type="{http://schemas.microsoft.com/exchange/services/2006/types}ConflictResultsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateItemResponseMessageType", propOrder = {
    "conflictResults"
})
public class UpdateItemResponseMessageType
    extends ItemInfoResponseMessageType
{

    @XmlElement(name = "ConflictResults")
    protected ConflictResultsType conflictResults;

    /**
     * Gets the value of the conflictResults property.
     * 
     * @return
     *     possible object is
     *     {@link ConflictResultsType }
     *     
     */
    public ConflictResultsType getConflictResults() {
        return conflictResults;
    }

    /**
     * Sets the value of the conflictResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConflictResultsType }
     *     
     */
    public void setConflictResults(ConflictResultsType value) {
        this.conflictResults = value;
    }

}
