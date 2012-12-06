
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfItemChangeDescriptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfItemChangeDescriptionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="AppendToItemField" type="{http://schemas.microsoft.com/exchange/services/2006/types}AppendToItemFieldType"/>
 *         &lt;element name="SetItemField" type="{http://schemas.microsoft.com/exchange/services/2006/types}SetItemFieldType"/>
 *         &lt;element name="DeleteItemField" type="{http://schemas.microsoft.com/exchange/services/2006/types}DeleteItemFieldType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfItemChangeDescriptionsType", propOrder = {
    "appendToItemFieldOrSetItemFieldOrDeleteItemField"
})
public class NonEmptyArrayOfItemChangeDescriptionsType {

    @XmlElements({
        @XmlElement(name = "SetItemField", type = SetItemFieldType.class),
        @XmlElement(name = "AppendToItemField", type = AppendToItemFieldType.class),
        @XmlElement(name = "DeleteItemField", type = DeleteItemFieldType.class)
    })
    protected List<ItemChangeDescriptionType> appendToItemFieldOrSetItemFieldOrDeleteItemField;

    /**
     * Gets the value of the appendToItemFieldOrSetItemFieldOrDeleteItemField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appendToItemFieldOrSetItemFieldOrDeleteItemField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppendToItemFieldOrSetItemFieldOrDeleteItemField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SetItemFieldType }
     * {@link AppendToItemFieldType }
     * {@link DeleteItemFieldType }
     * 
     * 
     */
    public List<ItemChangeDescriptionType> getAppendToItemFieldOrSetItemFieldOrDeleteItemField() {
        if (appendToItemFieldOrSetItemFieldOrDeleteItemField == null) {
            appendToItemFieldOrSetItemFieldOrDeleteItemField = new ArrayList<ItemChangeDescriptionType>();
        }
        return this.appendToItemFieldOrSetItemFieldOrDeleteItemField;
    }

}
