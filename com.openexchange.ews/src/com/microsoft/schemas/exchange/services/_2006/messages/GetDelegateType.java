
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfUserIdType;


/**
 * <p>Java class for GetDelegateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetDelegateType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseDelegateType">
 *       &lt;sequence>
 *         &lt;element name="UserIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfUserIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="IncludePermissions" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDelegateType", propOrder = {
    "userIds"
})
public class GetDelegateType
    extends BaseDelegateType
{

    @XmlElement(name = "UserIds")
    protected ArrayOfUserIdType userIds;
    @XmlAttribute(name = "IncludePermissions", required = true)
    protected boolean includePermissions;

    /**
     * Gets the value of the userIds property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfUserIdType }
     *     
     */
    public ArrayOfUserIdType getUserIds() {
        return userIds;
    }

    /**
     * Sets the value of the userIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfUserIdType }
     *     
     */
    public void setUserIds(ArrayOfUserIdType value) {
        this.userIds = value;
    }

    /**
     * Gets the value of the includePermissions property.
     * 
     */
    public boolean isIncludePermissions() {
        return includePermissions;
    }

    /**
     * Sets the value of the includePermissions property.
     * 
     */
    public void setIncludePermissions(boolean value) {
        this.includePermissions = value;
    }

}
