
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUInt;


/**
 * Conference extension data
 * 
 * <p>Java class for ConferenceInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConferenceInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneCallerIDInfo">
 *       &lt;sequence>
 *         &lt;element name="connectionSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="connectionSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="size" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="record" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="storage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="mohActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="mohFolder" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="close" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="announceUserCount" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="announceUser" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConferenceInfo", propOrder = {
    "connectionSndActive",
    "connectionSnd",
    "size",
    "record",
    "storage",
    "mohActive",
    "mohFolder",
    "close",
    "announceUserCount",
    "announceUser"
})
public class ConferenceInfo
    extends PhoneCallerIDInfo
{

    protected Boolean connectionSndActive;
    protected String connectionSnd;
    protected BigInteger size;
    @XmlElement(defaultValue = "0")
    protected BigInteger record;
    protected UnlimitedUInt storage;
    protected Boolean mohActive;
    @XmlElementRef(name = "mohFolder", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<String> mohFolder;
    protected Boolean close;
    protected Boolean announceUserCount;
    protected Boolean announceUser;

    /**
     * Gets the value of the connectionSndActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isConnectionSndActive() {
        return connectionSndActive;
    }

    /**
     * Sets the value of the connectionSndActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConnectionSndActive(Boolean value) {
        this.connectionSndActive = value;
    }

    /**
     * Gets the value of the connectionSnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionSnd() {
        return connectionSnd;
    }

    /**
     * Sets the value of the connectionSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionSnd(String value) {
        this.connectionSnd = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSize(BigInteger value) {
        this.size = value;
    }

    /**
     * Gets the value of the record property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRecord() {
        return record;
    }

    /**
     * Sets the value of the record property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRecord(BigInteger value) {
        this.record = value;
    }

    /**
     * Gets the value of the storage property.
     * 
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *     
     */
    public UnlimitedUInt getStorage() {
        return storage;
    }

    /**
     * Sets the value of the storage property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *     
     */
    public void setStorage(UnlimitedUInt value) {
        this.storage = value;
    }

    /**
     * Gets the value of the mohActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMohActive() {
        return mohActive;
    }

    /**
     * Sets the value of the mohActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMohActive(Boolean value) {
        this.mohActive = value;
    }

    /**
     * Gets the value of the mohFolder property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMohFolder() {
        return mohFolder;
    }

    /**
     * Sets the value of the mohFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMohFolder(JAXBElement<String> value) {
        this.mohFolder = value;
    }

    /**
     * Gets the value of the close property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isClose() {
        return close;
    }

    /**
     * Sets the value of the close property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClose(Boolean value) {
        this.close = value;
    }

    /**
     * Gets the value of the announceUserCount property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAnnounceUserCount() {
        return announceUserCount;
    }

    /**
     * Sets the value of the announceUserCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnnounceUserCount(Boolean value) {
        this.announceUserCount = value;
    }

    /**
     * Gets the value of the announceUser property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAnnounceUser() {
        return announceUser;
    }

    /**
     * Sets the value of the announceUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnnounceUser(Boolean value) {
        this.announceUser = value;
    }

}
