
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.SharingDataType;


/**
 * <p>Java class for GetSharingFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSharingFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="SmtpAddress" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType"/>
 *         &lt;element name="DataType" type="{http://schemas.microsoft.com/exchange/services/2006/types}SharingDataType" minOccurs="0"/>
 *         &lt;element name="SharedFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyStringType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSharingFolderType", propOrder = {
    "smtpAddress",
    "dataType",
    "sharedFolderId"
})
public class GetSharingFolderType
    extends BaseRequestType
{

    @XmlElement(name = "SmtpAddress", required = true)
    protected String smtpAddress;
    @XmlElement(name = "DataType")
    protected SharingDataType dataType;
    @XmlElement(name = "SharedFolderId")
    protected String sharedFolderId;

    /**
     * Gets the value of the smtpAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmtpAddress() {
        return smtpAddress;
    }

    /**
     * Sets the value of the smtpAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmtpAddress(String value) {
        this.smtpAddress = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link SharingDataType }
     *     
     */
    public SharingDataType getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharingDataType }
     *     
     */
    public void setDataType(SharingDataType value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the sharedFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSharedFolderId() {
        return sharedFolderId;
    }

    /**
     * Sets the value of the sharedFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSharedFolderId(String value) {
        this.sharedFolderId = value;
    }

}
