
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
 *         &lt;element name="contextGroup" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "credentials"
})
@XmlRootElement(name = "listClients")
public class ListClients {

    @XmlElement(required = true, nillable = true)
    protected String contextGroup;

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

}
