
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EncryptedSharedFolderDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncryptedSharedFolderDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Token" type="{http://schemas.microsoft.com/exchange/services/2006/types}EncryptedDataContainerType"/>
 *         &lt;element name="Data" type="{http://schemas.microsoft.com/exchange/services/2006/types}EncryptedDataContainerType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EncryptedSharedFolderDataType", propOrder = {
    "token",
    "data"
})
public class EncryptedSharedFolderDataType {

    @XmlElement(name = "Token", required = true)
    protected EncryptedDataContainerType token;
    @XmlElement(name = "Data", required = true)
    protected EncryptedDataContainerType data;

    /**
     * Gets the value of the token property.
     * 
     * @return
     *     possible object is
     *     {@link EncryptedDataContainerType }
     *     
     */
    public EncryptedDataContainerType getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncryptedDataContainerType }
     *     
     */
    public void setToken(EncryptedDataContainerType value) {
        this.token = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link EncryptedDataContainerType }
     *     
     */
    public EncryptedDataContainerType getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncryptedDataContainerType }
     *     
     */
    public void setData(EncryptedDataContainerType value) {
        this.data = value;
    }

}
