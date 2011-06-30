
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.pbxdata_xsd._2_5.DeviceInfo;


/**
 * Phone provisioning data
 * 
 * <p>Java class for ProvisioningInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProvisioningInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}DeviceInfo">
 *       &lt;sequence>
 *         &lt;element name="provision" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="regenerate" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;sequence>
 *           &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password" minOccurs="0"/>
 *           &lt;element name="dtmf" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="rfc2833"/>
 *                 &lt;enumeration value="inband"/>
 *                 &lt;enumeration value="info"/>
 *                 &lt;enumeration value="auto"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="PBXConnected" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="qualify" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="codecs" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;choice>
 *             &lt;element name="lockIP" type="{http://4psa.com/Common.xsd/2.5.1}ip" maxOccurs="unbounded" minOccurs="0"/>
 *             &lt;sequence>
 *               &lt;element name="host" type="{http://4psa.com/Common.xsd/2.5.1}ip" minOccurs="0"/>
 *               &lt;element name="port" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *               &lt;element name="register" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *             &lt;/sequence>
 *           &lt;/choice>
 *           &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="publishStatus" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="allowReInvite" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="forceMWI" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvisioningInfo", propOrder = {
    "provision",
    "regenerate",
    "id",
    "identifier",
    "password",
    "dtmf",
    "pbxConnected",
    "qualify",
    "codecs",
    "lockIP",
    "host",
    "port",
    "register",
    "description",
    "publishStatus",
    "allowReInvite",
    "forceMWI"
})
public class ProvisioningInfo
    extends DeviceInfo
{

    protected Boolean provision;
    protected Boolean regenerate;
    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;
    protected String password;
    @XmlElement(defaultValue = "rfc2833")
    protected String dtmf;
    @XmlElement(name = "PBXConnected")
    protected Boolean pbxConnected;
    protected Boolean qualify;
    protected List<BigInteger> codecs;
    protected List<String> lockIP;
    protected String host;
    protected BigInteger port;
    protected Boolean register;
    protected String description;
    protected Boolean publishStatus;
    protected Boolean allowReInvite;
    protected Boolean forceMWI;

    /**
     * Gets the value of the provision property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isProvision() {
        return provision;
    }

    /**
     * Sets the value of the provision property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProvision(Boolean value) {
        this.provision = value;
    }

    /**
     * Gets the value of the regenerate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRegenerate() {
        return regenerate;
    }

    /**
     * Sets the value of the regenerate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRegenerate(Boolean value) {
        this.regenerate = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the dtmf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDtmf() {
        return dtmf;
    }

    /**
     * Sets the value of the dtmf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDtmf(String value) {
        this.dtmf = value;
    }

    /**
     * Gets the value of the pbxConnected property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPBXConnected() {
        return pbxConnected;
    }

    /**
     * Sets the value of the pbxConnected property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPBXConnected(Boolean value) {
        this.pbxConnected = value;
    }

    /**
     * Gets the value of the qualify property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isQualify() {
        return qualify;
    }

    /**
     * Sets the value of the qualify property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQualify(Boolean value) {
        this.qualify = value;
    }

    /**
     * Gets the value of the codecs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codecs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodecs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<BigInteger> getCodecs() {
        if (codecs == null) {
            codecs = new ArrayList<BigInteger>();
        }
        return this.codecs;
    }

    /**
     * Gets the value of the lockIP property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lockIP property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLockIP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLockIP() {
        if (lockIP == null) {
            lockIP = new ArrayList<String>();
        }
        return this.lockIP;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPort(BigInteger value) {
        this.port = value;
    }

    /**
     * Gets the value of the register property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRegister() {
        return register;
    }

    /**
     * Sets the value of the register property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRegister(Boolean value) {
        this.register = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the publishStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPublishStatus() {
        return publishStatus;
    }

    /**
     * Sets the value of the publishStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublishStatus(Boolean value) {
        this.publishStatus = value;
    }

    /**
     * Gets the value of the allowReInvite property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAllowReInvite() {
        return allowReInvite;
    }

    /**
     * Sets the value of the allowReInvite property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowReInvite(Boolean value) {
        this.allowReInvite = value;
    }

    /**
     * Gets the value of the forceMWI property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceMWI() {
        return forceMWI;
    }

    /**
     * Sets the value of the forceMWI property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceMWI(Boolean value) {
        this.forceMWI = value;
    }

}
