
package com._4psa.accountmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *           &lt;element name="login" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
 *         &lt;element name="linkResourceID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="linkUUID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="forceUpdate" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
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
    "id",
    "identifier",
    "login",
    "linkResourceID",
    "linkUUID",
    "forceUpdate"
})
@XmlRootElement(name = "LinkAccountRequest")
public class LinkAccountRequest {

    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;
    protected String login;
    @XmlElement(defaultValue = "1")
    protected BigInteger linkResourceID;
    protected String linkUUID;
    protected Boolean forceUpdate;

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
     * Gets the value of the linkResourceID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getLinkResourceID() {
        return linkResourceID;
    }

    /**
     * Sets the value of the linkResourceID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setLinkResourceID(BigInteger value) {
        this.linkResourceID = value;
    }

    /**
     * Gets the value of the linkUUID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLinkUUID() {
        return linkUUID;
    }

    /**
     * Sets the value of the linkUUID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLinkUUID(String value) {
        this.linkUUID = value;
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

}
