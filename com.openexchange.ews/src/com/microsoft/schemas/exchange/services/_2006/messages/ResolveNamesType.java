
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.schemas.exchange.services._2006.types.ResolveNamesSearchScopeType;


/**
 * <p>Java class for ResolveNamesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResolveNamesType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ParentFolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType" minOccurs="0"/>
 *         &lt;element name="UnresolvedEntry" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ReturnFullContactData" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="SearchScope" type="{http://schemas.microsoft.com/exchange/services/2006/types}ResolveNamesSearchScopeType" default="ActiveDirectoryContacts" />
 *       &lt;attribute name="ContactDataShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}DefaultShapeNamesType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResolveNamesType", propOrder = {
    "parentFolderIds",
    "unresolvedEntry"
})
public class ResolveNamesType
    extends BaseRequestType
{

    @XmlElement(name = "ParentFolderIds")
    protected NonEmptyArrayOfBaseFolderIdsType parentFolderIds;
    @XmlElement(name = "UnresolvedEntry", required = true)
    protected String unresolvedEntry;
    @XmlAttribute(name = "ReturnFullContactData", required = true)
    protected boolean returnFullContactData;
    @XmlAttribute(name = "SearchScope")
    protected ResolveNamesSearchScopeType searchScope;
    @XmlAttribute(name = "ContactDataShape")
    protected DefaultShapeNamesType contactDataShape;

    /**
     * Gets the value of the parentFolderIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public NonEmptyArrayOfBaseFolderIdsType getParentFolderIds() {
        return parentFolderIds;
    }

    /**
     * Sets the value of the parentFolderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public void setParentFolderIds(NonEmptyArrayOfBaseFolderIdsType value) {
        this.parentFolderIds = value;
    }

    /**
     * Gets the value of the unresolvedEntry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnresolvedEntry() {
        return unresolvedEntry;
    }

    /**
     * Sets the value of the unresolvedEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnresolvedEntry(String value) {
        this.unresolvedEntry = value;
    }

    /**
     * Gets the value of the returnFullContactData property.
     * 
     */
    public boolean isReturnFullContactData() {
        return returnFullContactData;
    }

    /**
     * Sets the value of the returnFullContactData property.
     * 
     */
    public void setReturnFullContactData(boolean value) {
        this.returnFullContactData = value;
    }

    /**
     * Gets the value of the searchScope property.
     * 
     * @return
     *     possible object is
     *     {@link ResolveNamesSearchScopeType }
     *     
     */
    public ResolveNamesSearchScopeType getSearchScope() {
        if (searchScope == null) {
            return ResolveNamesSearchScopeType.ACTIVE_DIRECTORY_CONTACTS;
        } else {
            return searchScope;
        }
    }

    /**
     * Sets the value of the searchScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResolveNamesSearchScopeType }
     *     
     */
    public void setSearchScope(ResolveNamesSearchScopeType value) {
        this.searchScope = value;
    }

    /**
     * Gets the value of the contactDataShape property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultShapeNamesType }
     *     
     */
    public DefaultShapeNamesType getContactDataShape() {
        return contactDataShape;
    }

    /**
     * Sets the value of the contactDataShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultShapeNamesType }
     *     
     */
    public void setContactDataShape(DefaultShapeNamesType value) {
        this.contactDataShape = value;
    }

}
