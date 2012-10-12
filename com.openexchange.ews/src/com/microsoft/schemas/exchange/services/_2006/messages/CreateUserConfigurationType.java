
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.UserConfigurationType;


/**
 * <p>Java class for CreateUserConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateUserConfigurationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="UserConfiguration" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateUserConfigurationType", propOrder = {
    "userConfiguration"
})
public class CreateUserConfigurationType
    extends BaseRequestType
{

    @XmlElement(name = "UserConfiguration", required = true)
    protected UserConfigurationType userConfiguration;

    /**
     * Gets the value of the userConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link UserConfigurationType }
     *     
     */
    public UserConfigurationType getUserConfiguration() {
        return userConfiguration;
    }

    /**
     * Sets the value of the userConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserConfigurationType }
     *     
     */
    public void setUserConfiguration(UserConfigurationType value) {
        this.userConfiguration = value;
    }

}
