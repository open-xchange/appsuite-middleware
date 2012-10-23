
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfFolderChangeDescriptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfFolderChangeDescriptionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="AppendToFolderField" type="{http://schemas.microsoft.com/exchange/services/2006/types}AppendToFolderFieldType"/>
 *         &lt;element name="SetFolderField" type="{http://schemas.microsoft.com/exchange/services/2006/types}SetFolderFieldType"/>
 *         &lt;element name="DeleteFolderField" type="{http://schemas.microsoft.com/exchange/services/2006/types}DeleteFolderFieldType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfFolderChangeDescriptionsType", propOrder = {
    "appendToFolderFieldOrSetFolderFieldOrDeleteFolderField"
})
public class NonEmptyArrayOfFolderChangeDescriptionsType {

    @XmlElements({
        @XmlElement(name = "DeleteFolderField", type = DeleteFolderFieldType.class),
        @XmlElement(name = "AppendToFolderField", type = AppendToFolderFieldType.class),
        @XmlElement(name = "SetFolderField", type = SetFolderFieldType.class)
    })
    protected List<FolderChangeDescriptionType> appendToFolderFieldOrSetFolderFieldOrDeleteFolderField;

    /**
     * Gets the value of the appendToFolderFieldOrSetFolderFieldOrDeleteFolderField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appendToFolderFieldOrSetFolderFieldOrDeleteFolderField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppendToFolderFieldOrSetFolderFieldOrDeleteFolderField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DeleteFolderFieldType }
     * {@link AppendToFolderFieldType }
     * {@link SetFolderFieldType }
     * 
     * 
     */
    public List<FolderChangeDescriptionType> getAppendToFolderFieldOrSetFolderFieldOrDeleteFolderField() {
        if (appendToFolderFieldOrSetFolderFieldOrDeleteFolderField == null) {
            appendToFolderFieldOrSetFolderFieldOrDeleteFolderField = new ArrayList<FolderChangeDescriptionType>();
        }
        return this.appendToFolderFieldOrSetFolderFieldOrDeleteFolderField;
    }

}
