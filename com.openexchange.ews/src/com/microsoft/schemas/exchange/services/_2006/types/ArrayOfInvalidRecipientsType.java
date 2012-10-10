
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfInvalidRecipientsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfInvalidRecipientsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="InvalidRecipient" type="{http://schemas.microsoft.com/exchange/services/2006/types}InvalidRecipientType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfInvalidRecipientsType", propOrder = {
    "invalidRecipient"
})
public class ArrayOfInvalidRecipientsType {

    @XmlElement(name = "InvalidRecipient")
    protected List<InvalidRecipientType> invalidRecipient;

    /**
     * Gets the value of the invalidRecipient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invalidRecipient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvalidRecipient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvalidRecipientType }
     * 
     * 
     */
    public List<InvalidRecipientType> getInvalidRecipient() {
        if (invalidRecipient == null) {
            invalidRecipient = new ArrayList<InvalidRecipientType>();
        }
        return this.invalidRecipient;
    }

}
