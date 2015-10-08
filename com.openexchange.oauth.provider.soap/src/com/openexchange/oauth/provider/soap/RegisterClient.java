
package com.openexchange.oauth.provider.soap;

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
 *         &lt;element name="clientData" type="{http://soap.provider.oauth.openexchange.com}ClientData"/>
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
    "contextGroup",
    "clientData",
    "credentials"
})
@XmlRootElement(name = "registerClient")
public class RegisterClient {

    @XmlElement(required = true, nillable = false)
    protected String contextGroup;
    @XmlElement(required = true, nillable = true)
    protected ClientData clientData;
    @XmlElement(required = true, nillable = true)
    protected Credentials credentials;

    /**
     * Gets the credentials
     *
     * @return The credentials
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials
     *
     * @param credentials The credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Gets the value of the groupId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContextGroup() {
        return contextGroup;
    }

    /**
     * Sets the value of the groupId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContextGroup(String value) {
        this.contextGroup = value;
    }

    /**
     * Gets the value of the clientData property.
     *
     * @return
     *     possible object is
     *     {@link ClientData }
     *
     */
    public ClientData getClientData() {
        return clientData;
    }

    /**
     * Sets the value of the clientData property.
     *
     * @param value
     *     allowed object is
     *     {@link ClientData }
     *
     */
    public void setClientData(ClientData value) {
        this.clientData = value;
    }

}
