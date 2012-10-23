
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfTransitionsGroupsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfTransitionsGroupsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TransitionsGroup" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTransitionsType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfTransitionsGroupsType", propOrder = {
    "transitionsGroup"
})
public class ArrayOfTransitionsGroupsType {

    @XmlElement(name = "TransitionsGroup", required = true)
    protected List<ArrayOfTransitionsType> transitionsGroup;

    /**
     * Gets the value of the transitionsGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transitionsGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransitionsGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ArrayOfTransitionsType }
     * 
     * 
     */
    public List<ArrayOfTransitionsType> getTransitionsGroup() {
        if (transitionsGroup == null) {
            transitionsGroup = new ArrayList<ArrayOfTransitionsType>();
        }
        return this.transitionsGroup;
    }

}
