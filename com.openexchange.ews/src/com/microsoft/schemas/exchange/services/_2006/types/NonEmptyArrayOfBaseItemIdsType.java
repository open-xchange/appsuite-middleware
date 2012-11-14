
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfBaseItemIdsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfBaseItemIdsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType"/>
 *         &lt;element name="OccurrenceItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}OccurrenceItemIdType"/>
 *         &lt;element name="RecurringMasterItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}RecurringMasterItemIdType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfBaseItemIdsType", propOrder = {
    "itemIdOrOccurrenceItemIdOrRecurringMasterItemId"
})
public class NonEmptyArrayOfBaseItemIdsType {

    @XmlElements({
        @XmlElement(name = "RecurringMasterItemId", type = RecurringMasterItemIdType.class),
        @XmlElement(name = "OccurrenceItemId", type = OccurrenceItemIdType.class),
        @XmlElement(name = "ItemId", type = ItemIdType.class)
    })
    protected List<BaseItemIdType> itemIdOrOccurrenceItemIdOrRecurringMasterItemId;

    /**
     * Gets the value of the itemIdOrOccurrenceItemIdOrRecurringMasterItemId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemIdOrOccurrenceItemIdOrRecurringMasterItemId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemIdOrOccurrenceItemIdOrRecurringMasterItemId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecurringMasterItemIdType }
     * {@link OccurrenceItemIdType }
     * {@link ItemIdType }
     * 
     * 
     */
    public List<BaseItemIdType> getItemIdOrOccurrenceItemIdOrRecurringMasterItemId() {
        if (itemIdOrOccurrenceItemIdOrRecurringMasterItemId == null) {
            itemIdOrOccurrenceItemIdOrRecurringMasterItemId = new ArrayList<BaseItemIdType>();
        }
        return this.itemIdOrOccurrenceItemIdOrRecurringMasterItemId;
    }

}
