
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The set of permissions on a folder
 * 
 * <p>Java class for PermissionSetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermissionSetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Permissions" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfPermissionsType"/>
 *         &lt;element name="UnknownEntries" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfUnknownEntriesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermissionSetType", propOrder = {
    "permissions",
    "unknownEntries"
})
public class PermissionSetType {

    @XmlElement(name = "Permissions", required = true)
    protected ArrayOfPermissionsType permissions;
    @XmlElement(name = "UnknownEntries")
    protected ArrayOfUnknownEntriesType unknownEntries;

    /**
     * Gets the value of the permissions property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfPermissionsType }
     *     
     */
    public ArrayOfPermissionsType getPermissions() {
        return permissions;
    }

    /**
     * Sets the value of the permissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfPermissionsType }
     *     
     */
    public void setPermissions(ArrayOfPermissionsType value) {
        this.permissions = value;
    }

    /**
     * Gets the value of the unknownEntries property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfUnknownEntriesType }
     *     
     */
    public ArrayOfUnknownEntriesType getUnknownEntries() {
        return unknownEntries;
    }

    /**
     * Sets the value of the unknownEntries property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfUnknownEntriesType }
     *     
     */
    public void setUnknownEntries(ArrayOfUnknownEntriesType value) {
        this.unknownEntries = value;
    }

}
