
package com._4psa.pbxdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.ProvisioningInfo;
import com._4psa.pbxmessages_xsd._2_5.AddDeviceRequest;
import com._4psa.pbxmessages_xsd._2_5.EditDeviceRequest;


/**
 * Phone provisioning data
 *
 * <p>Java class for DeviceInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DeviceInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="IP" type="{http://4psa.com/Common.xsd/2.5.1}ip" minOccurs="0"/>
 *         &lt;element name="phoneModel" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="firmware" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="firmwareVersion" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="phoneMAC" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="adminUsername" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="adminPass" type="{http://4psa.com/Common.xsd/2.5.1}password" minOccurs="0"/>
 *         &lt;element name="updateInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="protocol" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="https"/>
 *               &lt;enumeration value="http"/>
 *               &lt;enumeration value="tftp"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="MACBased" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="tplID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="passType" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="notes" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="connectionType" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="connectionIP" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="mask" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="gateway" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceInfo", propOrder = {
    "name",
    "ip",
    "phoneModel",
    "firmware",
    "firmwareVersion",
    "phoneMAC",
    "adminUsername",
    "adminPass",
    "updateInterval",
    "protocol",
    "macBased",
    "tplID",
    "passType",
    "notes",
    "status",
    "connectionType",
    "connectionIP",
    "mask",
    "gateway"
})
@XmlSeeAlso({
    ProvisioningInfo.class,
    AddDeviceRequest.class,
    EditDeviceRequest.class
})
public class DeviceInfo {

    protected Object name;
    @XmlElement(name = "IP")
    protected String ip;
    protected String phoneModel;
    protected String firmware;
    protected String firmwareVersion;
    protected String phoneMAC;
    protected String adminUsername;
    protected String adminPass;
    @XmlElement(defaultValue = "10")
    protected BigInteger updateInterval;
    protected String protocol;
    @XmlElement(name = "MACBased")
    protected Boolean macBased;
    protected BigInteger tplID;
    protected BigInteger passType;
    protected String notes;
    @XmlElement(defaultValue = "true")
    protected Boolean status;
    protected String connectionType;
    protected String connectionIP;
    protected String mask;
    protected String gateway;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link Object }
     *
     */
    public Object getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link Object }
     *
     */
    public void setName(Object value) {
        this.name = value;
    }

    /**
     * Gets the value of the ip property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIP() {
        return ip;
    }

    /**
     * Sets the value of the ip property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIP(String value) {
        this.ip = value;
    }

    /**
     * Gets the value of the phoneModel property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhoneModel() {
        return phoneModel;
    }

    /**
     * Sets the value of the phoneModel property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhoneModel(String value) {
        this.phoneModel = value;
    }

    /**
     * Gets the value of the firmware property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Sets the value of the firmware property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFirmware(String value) {
        this.firmware = value;
    }

    /**
     * Gets the value of the firmwareVersion property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Sets the value of the firmwareVersion property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFirmwareVersion(String value) {
        this.firmwareVersion = value;
    }

    /**
     * Gets the value of the phoneMAC property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhoneMAC() {
        return phoneMAC;
    }

    /**
     * Sets the value of the phoneMAC property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhoneMAC(String value) {
        this.phoneMAC = value;
    }

    /**
     * Gets the value of the adminUsername property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAdminUsername() {
        return adminUsername;
    }

    /**
     * Sets the value of the adminUsername property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAdminUsername(String value) {
        this.adminUsername = value;
    }

    /**
     * Gets the value of the adminPass property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAdminPass() {
        return adminPass;
    }

    /**
     * Sets the value of the adminPass property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAdminPass(String value) {
        this.adminPass = value;
    }

    /**
     * Gets the value of the updateInterval property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Sets the value of the updateInterval property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUpdateInterval(BigInteger value) {
        this.updateInterval = value;
    }

    /**
     * Gets the value of the protocol property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the macBased property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMACBased() {
        return macBased;
    }

    /**
     * Sets the value of the macBased property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMACBased(Boolean value) {
        this.macBased = value;
    }

    /**
     * Gets the value of the tplID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTplID() {
        return tplID;
    }

    /**
     * Sets the value of the tplID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTplID(BigInteger value) {
        this.tplID = value;
    }

    /**
     * Gets the value of the passType property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getPassType() {
        return passType;
    }

    /**
     * Sets the value of the passType property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setPassType(BigInteger value) {
        this.passType = value;
    }

    /**
     * Gets the value of the notes property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setStatus(Boolean value) {
        this.status = value;
    }

    /**
     * Gets the value of the connectionType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Sets the value of the connectionType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setConnectionType(String value) {
        this.connectionType = value;
    }

    /**
     * Gets the value of the connectionIP property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getConnectionIP() {
        return connectionIP;
    }

    /**
     * Sets the value of the connectionIP property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setConnectionIP(String value) {
        this.connectionIP = value;
    }

    /**
     * Gets the value of the mask property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMask() {
        return mask;
    }

    /**
     * Sets the value of the mask property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMask(String value) {
        this.mask = value;
    }

    /**
     * Gets the value of the gateway property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Sets the value of the gateway property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGateway(String value) {
        this.gateway = value;
    }

}
