
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfDelegateUserType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfDelegateUserType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DelegateUser" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateUserType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfDelegateUserType", propOrder = {
    "delegateUser"
})
public class ArrayOfDelegateUserType {

    @XmlElement(name = "DelegateUser", required = true)
    protected List<DelegateUserType> delegateUser;

    /**
     * Gets the value of the delegateUser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the delegateUser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDelegateUser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DelegateUserType }
     * 
     * 
     */
    public List<DelegateUserType> getDelegateUser() {
        if (delegateUser == null) {
            delegateUser = new ArrayList<DelegateUserType>();
        }
        return this.delegateUser;
    }

}
