
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A permission on a folder
 * 
 * <p>Java class for PermissionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermissionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePermissionType">
 *       &lt;sequence>
 *         &lt;element name="ReadItems" type="{http://schemas.microsoft.com/exchange/services/2006/types}PermissionReadAccessType" minOccurs="0"/>
 *         &lt;element name="PermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}PermissionLevelType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermissionType", propOrder = {
    "readItems",
    "permissionLevel"
})
public class PermissionType
    extends BasePermissionType
{

    @XmlElement(name = "ReadItems")
    protected PermissionReadAccessType readItems;
    @XmlElement(name = "PermissionLevel", required = true)
    protected PermissionLevelType permissionLevel;

    /**
     * Gets the value of the readItems property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionReadAccessType }
     *     
     */
    public PermissionReadAccessType getReadItems() {
        return readItems;
    }

    /**
     * Sets the value of the readItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionReadAccessType }
     *     
     */
    public void setReadItems(PermissionReadAccessType value) {
        this.readItems = value;
    }

    /**
     * Gets the value of the permissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link PermissionLevelType }
     *     
     */
    public PermissionLevelType getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * Sets the value of the permissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionLevelType }
     *     
     */
    public void setPermissionLevel(PermissionLevelType value) {
        this.permissionLevel = value;
    }

}
