
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
    "access"
})
@XmlRootElement(name = "GetSystemAPISettingsResponse")
public class GetSystemAPISettingsResponse {

    @XmlElement(required = true)
    protected GetSystemAPISettingsResponse.Access access;

    /**
     * Gets the value of the access property.
     * 
     * @return
     *     possible object is
     *     {@link GetSystemAPISettingsResponse.Access }
     *     
     */
    public GetSystemAPISettingsResponse.Access getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSystemAPISettingsResponse.Access }
     *     
     */
    public void setAccess(GetSystemAPISettingsResponse.Access value) {
        this.access = value;
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

}
