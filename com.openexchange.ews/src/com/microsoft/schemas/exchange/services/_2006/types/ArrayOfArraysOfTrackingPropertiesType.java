
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfArraysOfTrackingPropertiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfArraysOfTrackingPropertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="ArrayOfTrackingPropertiesType" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTrackingPropertiesType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfArraysOfTrackingPropertiesType", propOrder = {
    "arrayOfTrackingPropertiesType"
})
public class ArrayOfArraysOfTrackingPropertiesType {

    @XmlElement(name = "ArrayOfTrackingPropertiesType")
    protected List<ArrayOfTrackingPropertiesType> arrayOfTrackingPropertiesType;

    /**
     * Gets the value of the arrayOfTrackingPropertiesType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arrayOfTrackingPropertiesType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArrayOfTrackingPropertiesType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ArrayOfTrackingPropertiesType }
     * 
     * 
     */
    public List<ArrayOfTrackingPropertiesType> getArrayOfTrackingPropertiesType() {
        if (arrayOfTrackingPropertiesType == null) {
            arrayOfTrackingPropertiesType = new ArrayList<ArrayOfTrackingPropertiesType>();
        }
        return this.arrayOfTrackingPropertiesType;
    }

}
