
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseFolderType">
 *       &lt;sequence>
 *         &lt;element name="PermissionSet" type="{http://schemas.microsoft.com/exchange/services/2006/types}PermissionSetType" minOccurs="0"/>
 *         &lt;element name="UnreadCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FolderType", propOrder = {
    "permissionSet",
    "unreadCount"
})
@XmlSeeAlso({
    TasksFolderType.class,
    SearchFolderType.class
})
public class FolderType
    extends BaseFolderType
{

    @XmlElement(name = "PermissionSet")
    protected PermissionSetType permissionSet;
    @XmlElement(name = "UnreadCount")
    protected Integer unreadCount;

    /**
     * Gets the value of the permissionSet property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionSetType }
     *     
     */
    public PermissionSetType getPermissionSet() {
        return permissionSet;
    }

    /**
     * Sets the value of the permissionSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionSetType }
     *     
     */
    public void setPermissionSet(PermissionSetType value) {
        this.permissionSet = value;
    }

    /**
     * Gets the value of the unreadCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUnreadCount() {
        return unreadCount;
    }

    /**
     * Sets the value of the unreadCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUnreadCount(Integer value) {
        this.unreadCount = value;
    }

}
