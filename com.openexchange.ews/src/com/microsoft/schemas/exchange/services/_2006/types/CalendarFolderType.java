
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseFolderType">
 *       &lt;sequence>
 *         &lt;element name="SharingEffectiveRights" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarPermissionReadAccessType" minOccurs="0"/>
 *         &lt;element name="PermissionSet" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarPermissionSetType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarFolderType", propOrder = {
    "sharingEffectiveRights",
    "permissionSet"
})
public class CalendarFolderType
    extends BaseFolderType
{

    @XmlElement(name = "SharingEffectiveRights")
    protected CalendarPermissionReadAccessType sharingEffectiveRights;
    @XmlElement(name = "PermissionSet")
    protected CalendarPermissionSetType permissionSet;

    /**
     * Gets the value of the sharingEffectiveRights property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarPermissionReadAccessType }
     *     
     */
    public CalendarPermissionReadAccessType getSharingEffectiveRights() {
        return sharingEffectiveRights;
    }

    /**
     * Sets the value of the sharingEffectiveRights property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarPermissionReadAccessType }
     *     
     */
    public void setSharingEffectiveRights(CalendarPermissionReadAccessType value) {
        this.sharingEffectiveRights = value;
    }

    /**
     * Gets the value of the permissionSet property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarPermissionSetType }
     *     
     */
    public CalendarPermissionSetType getPermissionSet() {
        return permissionSet;
    }

    /**
     * Sets the value of the permissionSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarPermissionSetType }
     *     
     */
    public void setPermissionSet(CalendarPermissionSetType value) {
        this.permissionSet = value;
    }

}
