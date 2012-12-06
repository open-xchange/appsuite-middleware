
package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfServiceConfigurationResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfServiceConfigurationResponseMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServiceConfigurationResponseMessageType" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ServiceConfigurationResponseMessageType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfServiceConfigurationResponseMessageType", propOrder = {
    "serviceConfigurationResponseMessageType"
})
public class ArrayOfServiceConfigurationResponseMessageType {

    @XmlElement(name = "ServiceConfigurationResponseMessageType", required = true)
    protected List<ServiceConfigurationResponseMessageType> serviceConfigurationResponseMessageType;

    /**
     * Gets the value of the serviceConfigurationResponseMessageType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceConfigurationResponseMessageType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceConfigurationResponseMessageType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceConfigurationResponseMessageType }
     * 
     * 
     */
    public List<ServiceConfigurationResponseMessageType> getServiceConfigurationResponseMessageType() {
        if (serviceConfigurationResponseMessageType == null) {
            serviceConfigurationResponseMessageType = new ArrayList<ServiceConfigurationResponseMessageType>();
        }
        return this.serviceConfigurationResponseMessageType;
    }

}
