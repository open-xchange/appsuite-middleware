
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfFieldOrdersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfFieldOrdersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FieldOrder" type="{http://schemas.microsoft.com/exchange/services/2006/types}FieldOrderType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfFieldOrdersType", propOrder = {
    "fieldOrder"
})
public class NonEmptyArrayOfFieldOrdersType {

    @XmlElement(name = "FieldOrder", required = true)
    protected List<FieldOrderType> fieldOrder;

    /**
     * Gets the value of the fieldOrder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldOrder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldOrder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldOrderType }
     * 
     * 
     */
    public List<FieldOrderType> getFieldOrder() {
        if (fieldOrder == null) {
            fieldOrder = new ArrayList<FieldOrderType>();
        }
        return this.fieldOrder;
    }

}
