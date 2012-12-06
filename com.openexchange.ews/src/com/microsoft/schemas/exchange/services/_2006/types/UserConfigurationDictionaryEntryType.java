
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserConfigurationDictionaryEntryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserConfigurationDictionaryEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DictionaryKey" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationDictionaryObjectType"/>
 *         &lt;element name="DictionaryValue" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationDictionaryObjectType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserConfigurationDictionaryEntryType", propOrder = {
    "dictionaryKey",
    "dictionaryValue"
})
public class UserConfigurationDictionaryEntryType {

    @XmlElement(name = "DictionaryKey", required = true)
    protected UserConfigurationDictionaryObjectType dictionaryKey;
    @XmlElement(name = "DictionaryValue", required = true, nillable = true)
    protected UserConfigurationDictionaryObjectType dictionaryValue;

    /**
     * Gets the value of the dictionaryKey property.
     * 
     * @return
     *     possible object is
     *     {@link UserConfigurationDictionaryObjectType }
     *     
     */
    public UserConfigurationDictionaryObjectType getDictionaryKey() {
        return dictionaryKey;
    }

    /**
     * Sets the value of the dictionaryKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserConfigurationDictionaryObjectType }
     *     
     */
    public void setDictionaryKey(UserConfigurationDictionaryObjectType value) {
        this.dictionaryKey = value;
    }

    /**
     * Gets the value of the dictionaryValue property.
     * 
     * @return
     *     possible object is
     *     {@link UserConfigurationDictionaryObjectType }
     *     
     */
    public UserConfigurationDictionaryObjectType getDictionaryValue() {
        return dictionaryValue;
    }

    /**
     * Sets the value of the dictionaryValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserConfigurationDictionaryObjectType }
     *     
     */
    public void setDictionaryValue(UserConfigurationDictionaryObjectType value) {
        this.dictionaryValue = value;
    }

}
