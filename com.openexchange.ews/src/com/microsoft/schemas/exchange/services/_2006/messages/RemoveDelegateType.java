
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfUserIdType;


/**
 * <p>Java class for RemoveDelegateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoveDelegateType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseDelegateType">
 *       &lt;sequence>
 *         &lt;element name="UserIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfUserIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoveDelegateType", propOrder = {
    "userIds"
})
public class RemoveDelegateType
    extends BaseDelegateType
{

    @XmlElement(name = "UserIds", required = true)
    protected ArrayOfUserIdType userIds;

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

}
