
package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfDelegateUserResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfDelegateUserResponseMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DelegateUserResponseMessageType" type="{http://schemas.microsoft.com/exchange/services/2006/messages}DelegateUserResponseMessageType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfDelegateUserResponseMessageType", propOrder = {
    "delegateUserResponseMessageType"
})
public class ArrayOfDelegateUserResponseMessageType {

    @XmlElement(name = "DelegateUserResponseMessageType", required = true)
    protected List<DelegateUserResponseMessageType> delegateUserResponseMessageType;

    /**
     * Gets the value of the delegateUserResponseMessageType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the delegateUserResponseMessageType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDelegateUserResponseMessageType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DelegateUserResponseMessageType }
     * 
     * 
     */
    public List<DelegateUserResponseMessageType> getDelegateUserResponseMessageType() {
        if (delegateUserResponseMessageType == null) {
            delegateUserResponseMessageType = new ArrayList<DelegateUserResponseMessageType>();
        }
        return this.delegateUserResponseMessageType;
    }

}
