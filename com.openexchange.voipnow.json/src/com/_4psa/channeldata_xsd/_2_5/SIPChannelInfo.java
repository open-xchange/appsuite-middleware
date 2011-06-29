
package com._4psa.channeldata_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * SIP channel definition data
 * 
 * <p>Java class for SIPChannelInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SIPChannelInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="hostname" type="{http://4psa.com/Common.xsd/2.5.1}ip" minOccurs="0"/>
 *         &lt;element name="inviteIP" type="{http://4psa.com/Common.xsd/2.5.1}ip" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="login" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password" minOccurs="0"/>
 *         &lt;element name="register" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="authUsername" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="concurentCalls" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="paid" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="flow" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="in"/>
 *               &lt;enumeration value="out"/>
 *               &lt;enumeration value="both"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="dtmf" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="rfc2833"/>
 *               &lt;enumeration value="inband"/>
 *               &lt;enumeration value="info"/>
 *               &lt;enumeration value="auto"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="behindNAT" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callerID" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="fromUser" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="fromDomain" type="{http://4psa.com/Common.xsd/2.5.1}domain" minOccurs="0"/>
 *         &lt;element name="authExt" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="didFromInvite" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="qualify" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="prefixCalls" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="trusted" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="trustRemotePartyID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="sendRemotePartyID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="sessionTimers" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="sessionExpire" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="sessionMinExpire" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="sessionRefresher" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="codecs" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded"/>
 *         &lt;element name="useMD5" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="notes" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SIPChannelInfo", propOrder = {
    "name",
    "hostname",
    "inviteIP",
    "login",
    "password",
    "register",
    "authUsername",
    "concurentCalls",
    "paid",
    "flow",
    "dtmf",
    "behindNAT",
    "callerID",
    "fromUser",
    "fromDomain",
    "authExt",
    "didFromInvite",
    "qualify",
    "prefixCalls",
    "trusted",
    "trustRemotePartyID",
    "sendRemotePartyID",
    "sessionTimers",
    "sessionExpire",
    "sessionMinExpire",
    "sessionRefresher",
    "codecs",
    "useMD5",
    "notes"
})
public class SIPChannelInfo {

    @XmlElement(required = true)
    protected String name;
    protected String hostname;
    protected List<String> inviteIP;
    protected String login;
    protected String password;
    protected Boolean register;
    protected String authUsername;
    @XmlElementRef(name = "concurentCalls", namespace = "http://4psa.com/ChannelData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigDecimal> concurentCalls;
    @XmlElement(defaultValue = "1")
    protected Boolean paid;
    @XmlElement(defaultValue = "both")
    protected String flow;
    @XmlElement(defaultValue = "rfc2833")
    protected String dtmf;
    @XmlElement(defaultValue = "0")
    protected Boolean behindNAT;
    protected BigDecimal callerID;
    protected String fromUser;
    protected String fromDomain;
    protected String authExt;
    protected String didFromInvite;
    @XmlElementRef(name = "qualify", namespace = "http://4psa.com/ChannelData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigDecimal> qualify;
    protected BigDecimal prefixCalls;
    @XmlElement(defaultValue = "0")
    protected Boolean trusted;
    @XmlElement(defaultValue = "0")
    protected Boolean trustRemotePartyID;
    @XmlElement(defaultValue = "0")
    protected Boolean sendRemotePartyID;
    @XmlElement(defaultValue = "0")
    protected String sessionTimers;
    @XmlElement(defaultValue = "0")
    protected BigInteger sessionExpire;
    @XmlElement(defaultValue = "0")
    protected BigInteger sessionMinExpire;
    @XmlElement(defaultValue = "0")
    protected String sessionRefresher;
    @XmlElement(required = true)
    protected List<BigInteger> codecs;
    @XmlElement(defaultValue = "0")
    protected Boolean useMD5;
    protected String notes;

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
     * Gets the value of the hostname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostname(String value) {
        this.hostname = value;
    }

    /**
     * Gets the value of the inviteIP property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inviteIP property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInviteIP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getInviteIP() {
        if (inviteIP == null) {
            inviteIP = new ArrayList<String>();
        }
        return this.inviteIP;
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
     * Gets the value of the authUsername property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthUsername() {
        return authUsername;
    }

    /**
     * Sets the value of the authUsername property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthUsername(String value) {
        this.authUsername = value;
    }

    /**
     * Gets the value of the concurentCalls property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public JAXBElement<BigDecimal> getConcurentCalls() {
        return concurentCalls;
    }

    /**
     * Sets the value of the concurentCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public void setConcurentCalls(JAXBElement<BigDecimal> value) {
        this.concurentCalls = ((JAXBElement<BigDecimal> ) value);
    }

    /**
     * Gets the value of the paid property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPaid() {
        return paid;
    }

    /**
     * Sets the value of the paid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPaid(Boolean value) {
        this.paid = value;
    }

    /**
     * Gets the value of the flow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlow() {
        return flow;
    }

    /**
     * Sets the value of the flow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlow(String value) {
        this.flow = value;
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
     * Gets the value of the behindNAT property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBehindNAT() {
        return behindNAT;
    }

    /**
     * Sets the value of the behindNAT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBehindNAT(Boolean value) {
        this.behindNAT = value;
    }

    /**
     * Gets the value of the callerID property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCallerID() {
        return callerID;
    }

    /**
     * Sets the value of the callerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCallerID(BigDecimal value) {
        this.callerID = value;
    }

    /**
     * Gets the value of the fromUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromUser() {
        return fromUser;
    }

    /**
     * Sets the value of the fromUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromUser(String value) {
        this.fromUser = value;
    }

    /**
     * Gets the value of the fromDomain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromDomain() {
        return fromDomain;
    }

    /**
     * Sets the value of the fromDomain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromDomain(String value) {
        this.fromDomain = value;
    }

    /**
     * Gets the value of the authExt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthExt() {
        return authExt;
    }

    /**
     * Sets the value of the authExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthExt(String value) {
        this.authExt = value;
    }

    /**
     * Gets the value of the didFromInvite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDidFromInvite() {
        return didFromInvite;
    }

    /**
     * Sets the value of the didFromInvite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDidFromInvite(String value) {
        this.didFromInvite = value;
    }

    /**
     * Gets the value of the qualify property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public JAXBElement<BigDecimal> getQualify() {
        return qualify;
    }

    /**
     * Sets the value of the qualify property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public void setQualify(JAXBElement<BigDecimal> value) {
        this.qualify = ((JAXBElement<BigDecimal> ) value);
    }

    /**
     * Gets the value of the prefixCalls property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPrefixCalls() {
        return prefixCalls;
    }

    /**
     * Sets the value of the prefixCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPrefixCalls(BigDecimal value) {
        this.prefixCalls = value;
    }

    /**
     * Gets the value of the trusted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTrusted() {
        return trusted;
    }

    /**
     * Sets the value of the trusted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTrusted(Boolean value) {
        this.trusted = value;
    }

    /**
     * Gets the value of the trustRemotePartyID property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTrustRemotePartyID() {
        return trustRemotePartyID;
    }

    /**
     * Sets the value of the trustRemotePartyID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTrustRemotePartyID(Boolean value) {
        this.trustRemotePartyID = value;
    }

    /**
     * Gets the value of the sendRemotePartyID property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSendRemotePartyID() {
        return sendRemotePartyID;
    }

    /**
     * Sets the value of the sendRemotePartyID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSendRemotePartyID(Boolean value) {
        this.sendRemotePartyID = value;
    }

    /**
     * Gets the value of the sessionTimers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionTimers() {
        return sessionTimers;
    }

    /**
     * Sets the value of the sessionTimers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionTimers(String value) {
        this.sessionTimers = value;
    }

    /**
     * Gets the value of the sessionExpire property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSessionExpire() {
        return sessionExpire;
    }

    /**
     * Sets the value of the sessionExpire property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSessionExpire(BigInteger value) {
        this.sessionExpire = value;
    }

    /**
     * Gets the value of the sessionMinExpire property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSessionMinExpire() {
        return sessionMinExpire;
    }

    /**
     * Sets the value of the sessionMinExpire property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSessionMinExpire(BigInteger value) {
        this.sessionMinExpire = value;
    }

    /**
     * Gets the value of the sessionRefresher property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionRefresher() {
        return sessionRefresher;
    }

    /**
     * Sets the value of the sessionRefresher property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionRefresher(String value) {
        this.sessionRefresher = value;
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
     * Gets the value of the useMD5 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUseMD5() {
        return useMD5;
    }

    /**
     * Sets the value of the useMD5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseMD5(Boolean value) {
        this.useMD5 = value;
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

}
