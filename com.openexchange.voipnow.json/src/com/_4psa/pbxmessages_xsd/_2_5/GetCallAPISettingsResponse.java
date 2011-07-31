
package com._4psa.pbxmessages_xsd._2_5;

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
 *         &lt;element name="access">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="legacyAuth" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                   &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="auth">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
 *                   &lt;element name="username" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "access",
    "auth"
})
@XmlRootElement(name = "GetCallAPISettingsResponse")
public class GetCallAPISettingsResponse {

    @XmlElement(required = true)
    protected GetCallAPISettingsResponse.Access access;
    @XmlElement(required = true)
    protected GetCallAPISettingsResponse.Auth auth;

    /**
     * Gets the value of the access property.
     *
     * @return
     *     possible object is
     *     {@link GetCallAPISettingsResponse.Access }
     *
     */
    public GetCallAPISettingsResponse.Access getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     *
     * @param value
     *     allowed object is
     *     {@link GetCallAPISettingsResponse.Access }
     *
     */
    public void setAccess(GetCallAPISettingsResponse.Access value) {
        this.access = value;
    }

    /**
     * Gets the value of the auth property.
     *
     * @return
     *     possible object is
     *     {@link GetCallAPISettingsResponse.Auth }
     *
     */
    public GetCallAPISettingsResponse.Auth getAuth() {
        return auth;
    }

    /**
     * Sets the value of the auth property.
     *
     * @param value
     *     allowed object is
     *     {@link GetCallAPISettingsResponse.Auth }
     *
     */
    public void setAuth(GetCallAPISettingsResponse.Auth value) {
        this.auth = value;
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
     *         &lt;element name="legacyAuth" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
        "legacyAuth",
        "status"
    })
    public static class Access {

        protected boolean legacyAuth;
        protected boolean status;

        /**
         * Gets the value of the legacyAuth property.
         *
         */
        public boolean isLegacyAuth() {
            return legacyAuth;
        }

        /**
         * Sets the value of the legacyAuth property.
         *
         */
        public void setLegacyAuth(boolean value) {
            this.legacyAuth = value;
        }

        /**
         * Gets the value of the status property.
         *
         */
        public boolean isStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         *
         */
        public void setStatus(boolean value) {
            this.status = value;
        }

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
     *         &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
     *         &lt;element name="username" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
        "password",
        "username"
    })
    public static class Auth {

        @XmlElement(required = true)
        protected String password;
        @XmlElement(required = true)
        protected String username;

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

    }

}
