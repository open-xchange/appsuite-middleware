
package com._4psa.clientdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientmessages_xsd._2_5.AddClientRequest;
import com._4psa.clientmessages_xsd._2_5.EditClientRequest;
import com._4psa.extensiondata_xsd._2_5.ExtensionInfo;
import com._4psa.resellerdata_xsd._2_5.ResellerInfo;


/**
 * Client account data
 *
 * <p>Java class for ClientInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ClientInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="company" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="login" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="passwordAuto" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password" minOccurs="0"/>
 *         &lt;element name="passwordStrength" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="low"/>
 *               &lt;enumeration value="medium"/>
 *               &lt;enumeration value="high"/>
 *               &lt;enumeration value="veryHigh"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="forceUpdate" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="phone" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="fax" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="email" type="{http://4psa.com/Common.xsd/2.5.1}email" minOccurs="0"/>
 *         &lt;element name="address" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="city" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="pcode" type="{http://4psa.com/Common.xsd/2.5.1}pcode" minOccurs="0"/>
 *         &lt;element name="country" type="{http://4psa.com/Common.xsd/2.5.1}code" minOccurs="0"/>
 *         &lt;element name="region" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="timezone" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="interfaceLang" type="{http://4psa.com/Common.xsd/2.5.1}code" minOccurs="0"/>
 *         &lt;element name="notes" type="{http://4psa.com/Common.xsd/2.5.1}text" minOccurs="0"/>
 *         &lt;element name="serverID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="chargingIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientInfo", propOrder = {
    "name",
    "company",
    "login",
    "passwordAuto",
    "password",
    "passwordStrength",
    "forceUpdate",
    "phone",
    "fax",
    "email",
    "address",
    "city",
    "pcode",
    "country",
    "region",
    "timezone",
    "interfaceLang",
    "notes",
    "serverID",
    "chargingIdentifier"
})
@XmlSeeAlso({
    EditClientRequest.class,
    AddClientRequest.class,
    ExtendedClientInfo.class,
    ResellerInfo.class,
    ExtensionInfo.class
})
public class ClientInfo {

    protected String name;
    protected String company;
    protected String login;
    protected Boolean passwordAuto;
    protected String password;
    protected String passwordStrength;
    protected Boolean forceUpdate;
    protected String phone;
    protected String fax;
    protected String email;
    protected String address;
    protected String city;
    protected String pcode;
    protected String country;
    protected BigInteger region;
    protected BigInteger timezone;
    protected String interfaceLang;
    protected String notes;
    protected String serverID;
    protected String chargingIdentifier;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the company property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCompany(String value) {
        this.company = value;
    }

    /**
     * Gets the value of the login property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Gets the value of the passwordAuto property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPasswordAuto() {
        return passwordAuto;
    }

    /**
     * Sets the value of the passwordAuto property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setPasswordAuto(Boolean value) {
        this.passwordAuto = value;
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
     * Gets the value of the passwordStrength property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPasswordStrength() {
        return passwordStrength;
    }

    /**
     * Sets the value of the passwordStrength property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPasswordStrength(String value) {
        this.passwordStrength = value;
    }

    /**
     * Gets the value of the forceUpdate property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isForceUpdate() {
        return forceUpdate;
    }

    /**
     * Sets the value of the forceUpdate property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setForceUpdate(Boolean value) {
        this.forceUpdate = value;
    }

    /**
     * Gets the value of the phone property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the fax property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFax() {
        return fax;
    }

    /**
     * Sets the value of the fax property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFax(String value) {
        this.fax = value;
    }

    /**
     * Gets the value of the email property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the address property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the city property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCity(String value) {
        this.city = value;
    }

    /**
     * Gets the value of the pcode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPcode() {
        return pcode;
    }

    /**
     * Sets the value of the pcode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPcode(String value) {
        this.pcode = value;
    }

    /**
     * Gets the value of the country property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the region property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getRegion() {
        return region;
    }

    /**
     * Sets the value of the region property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setRegion(BigInteger value) {
        this.region = value;
    }

    /**
     * Gets the value of the timezone property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTimezone(BigInteger value) {
        this.timezone = value;
    }

    /**
     * Gets the value of the interfaceLang property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInterfaceLang() {
        return interfaceLang;
    }

    /**
     * Sets the value of the interfaceLang property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInterfaceLang(String value) {
        this.interfaceLang = value;
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
     * Gets the value of the serverID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getServerID() {
        return serverID;
    }

    /**
     * Sets the value of the serverID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setServerID(String value) {
        this.serverID = value;
    }

    /**
     * Gets the value of the chargingIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getChargingIdentifier() {
        return chargingIdentifier;
    }

    /**
     * Sets the value of the chargingIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setChargingIdentifier(String value) {
        this.chargingIdentifier = value;
    }

}
