
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         User Mailbox.
 *       
 * 
 * <p>Java class for UserMailboxType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserMailboxType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsArchive" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserMailboxType")
public class UserMailboxType {

    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "IsArchive", required = true)
    protected boolean isArchive;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the isArchive property.
     * 
     */
    public boolean isIsArchive() {
        return isArchive;
    }

    /**
     * Sets the value of the isArchive property.
     * 
     */
    public void setIsArchive(boolean value) {
        this.isArchive = value;
    }

}
