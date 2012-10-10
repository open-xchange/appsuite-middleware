
package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfMailTipsResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfMailTipsResponseMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MailTipsResponseMessageType" type="{http://schemas.microsoft.com/exchange/services/2006/messages}MailTipsResponseMessageType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfMailTipsResponseMessageType", propOrder = {
    "mailTipsResponseMessageType"
})
public class ArrayOfMailTipsResponseMessageType {

    @XmlElement(name = "MailTipsResponseMessageType", required = true)
    protected List<MailTipsResponseMessageType> mailTipsResponseMessageType;

    /**
     * Gets the value of the mailTipsResponseMessageType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mailTipsResponseMessageType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMailTipsResponseMessageType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MailTipsResponseMessageType }
     * 
     * 
     */
    public List<MailTipsResponseMessageType> getMailTipsResponseMessageType() {
        if (mailTipsResponseMessageType == null) {
            mailTipsResponseMessageType = new ArrayList<MailTipsResponseMessageType>();
        }
        return this.mailTipsResponseMessageType;
    }

}
