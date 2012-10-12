
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfAttachmentsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfAttachmentsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="ItemAttachment" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemAttachmentType"/>
 *         &lt;element name="FileAttachment" type="{http://schemas.microsoft.com/exchange/services/2006/types}FileAttachmentType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfAttachmentsType", propOrder = {
    "itemAttachmentOrFileAttachment"
})
public class NonEmptyArrayOfAttachmentsType {

    @XmlElements({
        @XmlElement(name = "ItemAttachment", type = ItemAttachmentType.class),
        @XmlElement(name = "FileAttachment", type = FileAttachmentType.class)
    })
    protected List<AttachmentType> itemAttachmentOrFileAttachment;

    /**
     * Gets the value of the itemAttachmentOrFileAttachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemAttachmentOrFileAttachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemAttachmentOrFileAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemAttachmentType }
     * {@link FileAttachmentType }
     * 
     * 
     */
    public List<AttachmentType> getItemAttachmentOrFileAttachment() {
        if (itemAttachmentOrFileAttachment == null) {
            itemAttachmentOrFileAttachment = new ArrayList<AttachmentType>();
        }
        return this.itemAttachmentOrFileAttachment;
    }

}
