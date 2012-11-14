
package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.UserConfigurationNameType;


/**
 * <p>Java class for GetUserConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUserConfigurationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="UserConfigurationName" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationNameType"/>
 *         &lt;element name="UserConfigurationProperties" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationPropertyType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUserConfigurationType", propOrder = {
    "userConfigurationName",
    "userConfigurationProperties"
})
public class GetUserConfigurationType
    extends BaseRequestType
{

    @XmlElement(name = "UserConfigurationName", required = true)
    protected UserConfigurationNameType userConfigurationName;
    @XmlList
    @XmlElement(name = "UserConfigurationProperties", required = true)
    protected List<String> userConfigurationProperties;

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

    /**
     * Gets the value of the userConfigurationProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userConfigurationProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserConfigurationProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUserConfigurationProperties() {
        if (userConfigurationProperties == null) {
            userConfigurationProperties = new ArrayList<String>();
        }
        return this.userConfigurationProperties;
    }

}
