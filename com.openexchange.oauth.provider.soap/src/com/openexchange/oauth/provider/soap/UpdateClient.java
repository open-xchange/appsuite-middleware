
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
 *         &lt;element name="clientId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "clientId",
    "clientData",
    "credentials"
})
@XmlRootElement(name = "updateClient")
public class UpdateClient {

    @XmlElement(required = true, nillable = true)
    protected String clientId;
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
     * Gets the value of the clientId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClientId(String value) {
        this.clientId = value;
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
