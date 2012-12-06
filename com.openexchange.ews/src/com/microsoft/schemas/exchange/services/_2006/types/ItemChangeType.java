
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ItemChangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemChangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *           &lt;element name="OccurrenceItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}OccurrenceItemIdType"/>
 *           &lt;element name="RecurringMasterItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}RecurringMasterItemIdType"/>
 *         &lt;/choice>
 *         &lt;element name="Updates" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfItemChangeDescriptionsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemChangeType", propOrder = {
    "itemId",
    "occurrenceItemId",
    "recurringMasterItemId",
    "updates"
})
public class ItemChangeType {

    @XmlElement(name = "ItemId")
    protected ItemIdType itemId;
    @XmlElement(name = "OccurrenceItemId")
    protected OccurrenceItemIdType occurrenceItemId;
    @XmlElement(name = "RecurringMasterItemId")
    protected RecurringMasterItemIdType recurringMasterItemId;
    @XmlElement(name = "Updates", required = true)
    protected NonEmptyArrayOfItemChangeDescriptionsType updates;

    /**
     * Gets the value of the itemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getItemId() {
        return itemId;
    }

    /**
     * Sets the value of the itemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setItemId(ItemIdType value) {
        this.itemId = value;
    }

    /**
     * Gets the value of the occurrenceItemId property.
     * 
     * @return
     *     possible object is
     *     {@link OccurrenceItemIdType }
     *     
     */
    public OccurrenceItemIdType getOccurrenceItemId() {
        return occurrenceItemId;
    }

    /**
     * Sets the value of the occurrenceItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link OccurrenceItemIdType }
     *     
     */
    public void setOccurrenceItemId(OccurrenceItemIdType value) {
        this.occurrenceItemId = value;
    }

    /**
     * Gets the value of the recurringMasterItemId property.
     * 
     * @return
     *     possible object is
     *     {@link RecurringMasterItemIdType }
     *     
     */
    public RecurringMasterItemIdType getRecurringMasterItemId() {
        return recurringMasterItemId;
    }

    /**
     * Sets the value of the recurringMasterItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecurringMasterItemIdType }
     *     
     */
    public void setRecurringMasterItemId(RecurringMasterItemIdType value) {
        this.recurringMasterItemId = value;
    }

    /**
     * Gets the value of the updates property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfItemChangeDescriptionsType }
     *     
     */
    public NonEmptyArrayOfItemChangeDescriptionsType getUpdates() {
        return updates;
    }

    /**
     * Sets the value of the updates property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfItemChangeDescriptionsType }
     *     
     */
    public void setUpdates(NonEmptyArrayOfItemChangeDescriptionsType value) {
        this.updates = value;
    }

}
