
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfSmtpAddressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSmtpAddressType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="SmtpAddress" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSmtpAddressType", propOrder = {
    "smtpAddress"
})
public class ArrayOfSmtpAddressType {

    @XmlElement(name = "SmtpAddress")
    protected List<String> smtpAddress;

    /**
     * Gets the value of the smtpAddress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smtpAddress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSmtpAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSmtpAddress() {
        if (smtpAddress == null) {
            smtpAddress = new ArrayList<String>();
        }
        return this.smtpAddress;
    }

}
