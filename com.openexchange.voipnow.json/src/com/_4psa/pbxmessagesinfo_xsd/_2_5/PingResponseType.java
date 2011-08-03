
package com._4psa.pbxmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Ping 4PSA VoipNow server: response type
 *
 * <p>Java class for PingResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PingResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="buildNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="resellers" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="clients" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="extensions" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="activSSO" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="availableUpdate" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="in progress"/>
 *               &lt;enumeration value="YES"/>
 *               &lt;enumeration value="NO"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="api" maxOccurs="5" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="protocol" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="version" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="infrastructureID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="charging" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="automationAgent" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PingResponseType", propOrder = {
    "title",
    "version",
    "buildNo",
    "resellers",
    "clients",
    "extensions",
    "activSSO",
    "availableUpdate",
    "api",
    "infrastructureID",
    "currency",
    "charging",
    "automationAgent"
})
public class PingResponseType {

    protected String title;
    protected String version;
    protected String buildNo;
    protected BigInteger resellers;
    protected BigInteger clients;
    protected BigInteger extensions;
    protected Boolean activSSO;
    protected String availableUpdate;
    protected List<PingResponseType.Api> api;
    protected String infrastructureID;
    protected String currency;
    protected Boolean charging;
    protected Boolean automationAgent;

    /**
     * Gets the value of the title property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the buildNo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBuildNo() {
        return buildNo;
    }

    /**
     * Sets the value of the buildNo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBuildNo(String value) {
        this.buildNo = value;
    }

    /**
     * Gets the value of the resellers property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getResellers() {
        return resellers;
    }

    /**
     * Sets the value of the resellers property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setResellers(BigInteger value) {
        this.resellers = value;
    }

    /**
     * Gets the value of the clients property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getClients() {
        return clients;
    }

    /**
     * Sets the value of the clients property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setClients(BigInteger value) {
        this.clients = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setExtensions(BigInteger value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the activSSO property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isActivSSO() {
        return activSSO;
    }

    /**
     * Sets the value of the activSSO property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setActivSSO(Boolean value) {
        this.activSSO = value;
    }

    /**
     * Gets the value of the availableUpdate property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAvailableUpdate() {
        return availableUpdate;
    }

    /**
     * Sets the value of the availableUpdate property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAvailableUpdate(String value) {
        this.availableUpdate = value;
    }

    /**
     * Gets the value of the api property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the api property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApi().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PingResponseType.Api }
     *
     *
     */
    public List<PingResponseType.Api> getApi() {
        if (api == null) {
            api = new ArrayList<PingResponseType.Api>();
        }
        return this.api;
    }

    /**
     * Gets the value of the infrastructureID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInfrastructureID() {
        return infrastructureID;
    }

    /**
     * Sets the value of the infrastructureID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInfrastructureID(String value) {
        this.infrastructureID = value;
    }

    /**
     * Gets the value of the currency property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the charging property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCharging() {
        return charging;
    }

    /**
     * Sets the value of the charging property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCharging(Boolean value) {
        this.charging = value;
    }

    /**
     * Gets the value of the automationAgent property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAutomationAgent() {
        return automationAgent;
    }

    /**
     * Sets the value of the automationAgent property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAutomationAgent(Boolean value) {
        this.automationAgent = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="protocol" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="version" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "protocol",
        "version"
    })
    public static class Api {

        @XmlElement(required = true)
        protected String protocol;
        @XmlElement(required = true)
        protected String version;

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
         * Gets the value of the version property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getVersion() {
            return version;
        }

        /**
         * Sets the value of the version property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setVersion(String value) {
            this.version = value;
        }

    }

}
