
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.UserConfigurationNameType;


/**
 * <p>Java class for DeleteUserConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteUserConfigurationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="UserConfigurationName" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationNameType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteUserConfigurationType", propOrder = {
    "userConfigurationName"
})
public class DeleteUserConfigurationType
    extends BaseRequestType
{

    @XmlElement(name = "UserConfigurationName", required = true)
    protected UserConfigurationNameType userConfigurationName;

    /**
     * Gets the value of the userConfigurationName property.
     * 
     * @return
     *     possible object is
     *     {@link UserConfigurationNameType }
     *     
     */
    public UserConfigurationNameType getUserConfigurationName() {
        return userConfigurationName;
    }

    /**
     * Sets the value of the userConfigurationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserConfigurationNameType }
     *     
     */
    public void setUserConfigurationName(UserConfigurationNameType value) {
        this.userConfigurationName = value;
    }

}
