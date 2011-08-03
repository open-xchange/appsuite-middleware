
package com._4psa.headerdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="username" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *           &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="cryptedAuth" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="appKey" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *           &lt;element name="appSecret" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "username",
    "password",
    "cryptedAuth",
    "appKey",
    "appSecret"
})
@XmlRootElement(name = "userCredentials")
public class UserCredentials {

    protected String username;
    protected String password;
    protected String cryptedAuth;
    protected String appKey;
    protected String appSecret;

    /**
     * Gets the value of the username property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUsername(String value) {
        this.username = value;
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
     * Gets the value of the cryptedAuth property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCryptedAuth() {
        return cryptedAuth;
    }

    /**
     * Sets the value of the cryptedAuth property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCryptedAuth(String value) {
        this.cryptedAuth = value;
    }

    /**
     * Gets the value of the appKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * Sets the value of the appKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAppKey(String value) {
        this.appKey = value;
    }

    /**
     * Gets the value of the appSecret property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * Sets the value of the appSecret property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAppSecret(String value) {
        this.appSecret = value;
    }

}
