
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DistributionListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistributionListType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FileAs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ContactSource" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContactSourceType" minOccurs="0"/>
 *         &lt;element name="Members" type="{http://schemas.microsoft.com/exchange/services/2006/types}MembersListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistributionListType", propOrder = {
    "displayName",
    "fileAs",
    "contactSource",
    "members"
})
public class DistributionListType
    extends ItemType
{

    @XmlElement(name = "DisplayName")
    protected String displayName;
    @XmlElement(name = "FileAs")
    protected String fileAs;
    @XmlElement(name = "ContactSource")
    protected ContactSourceType contactSource;
    @XmlElement(name = "Members")
    protected MembersListType members;

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the fileAs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileAs() {
        return fileAs;
    }

    /**
     * Sets the value of the fileAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileAs(String value) {
        this.fileAs = value;
    }

    /**
     * Gets the value of the contactSource property.
     * 
     * @return
     *     possible object is
     *     {@link ContactSourceType }
     *     
     */
    public ContactSourceType getContactSource() {
        return contactSource;
    }

    /**
     * Sets the value of the contactSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactSourceType }
     *     
     */
    public void setContactSource(ContactSourceType value) {
        this.contactSource = value;
    }

    /**
     * Gets the value of the members property.
     * 
     * @return
     *     possible object is
     *     {@link MembersListType }
     *     
     */
    public MembersListType getMembers() {
        return members;
    }

    /**
     * Sets the value of the members property.
     * 
     * @param value
     *     allowed object is
     *     {@link MembersListType }
     *     
     */
    public void setMembers(MembersListType value) {
        this.members = value;
    }

}
