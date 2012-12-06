
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserConfigurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserConfigurationName" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationNameType"/>
 *         &lt;element name="ItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *         &lt;element name="Dictionary" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserConfigurationDictionaryType" minOccurs="0"/>
 *         &lt;element name="XmlData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="BinaryData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserConfigurationType", propOrder = {
    "userConfigurationName",
    "itemId",
    "dictionary",
    "xmlData",
    "binaryData"
})
public class UserConfigurationType {

    @XmlElement(name = "UserConfigurationName", required = true)
    protected UserConfigurationNameType userConfigurationName;
    @XmlElement(name = "ItemId")
    protected ItemIdType itemId;
    @XmlElement(name = "Dictionary")
    protected UserConfigurationDictionaryType dictionary;
    @XmlElement(name = "XmlData")
    protected byte[] xmlData;
    @XmlElement(name = "BinaryData")
    protected byte[] binaryData;

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
     * Gets the value of the itemId property.
     * 
     * @return
     *     possible object is
     *     {@link ItemIdType }
     *     
     */
    public ItemIdType getItemId() {
        return itemId;
    }

    /**
     * Sets the value of the itemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemIdType }
     *     
     */
    public void setItemId(ItemIdType value) {
        this.itemId = value;
    }

    /**
     * Gets the value of the dictionary property.
     * 
     * @return
     *     possible object is
     *     {@link UserConfigurationDictionaryType }
     *     
     */
    public UserConfigurationDictionaryType getDictionary() {
        return dictionary;
    }

    /**
     * Sets the value of the dictionary property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserConfigurationDictionaryType }
     *     
     */
    public void setDictionary(UserConfigurationDictionaryType value) {
        this.dictionary = value;
    }

    /**
     * Gets the value of the xmlData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getXmlData() {
        return xmlData;
    }

    /**
     * Sets the value of the xmlData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setXmlData(byte[] value) {
        this.xmlData = ((byte[]) value);
    }

    /**
     * Gets the value of the binaryData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBinaryData() {
        return binaryData;
    }

    /**
     * Sets the value of the binaryData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBinaryData(byte[] value) {
        this.binaryData = ((byte[]) value);
    }

}
