
package com.openexchange.oauth.provider.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f&uuml;r anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clientId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clientData" type="{http://soap.provider.oauth.openexchange.com}ClientData" minOccurs="0"/>
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
    "clientData"
})
@XmlRootElement(name = "updateClient")
public class UpdateClient {

    @XmlElement(nillable = true)
    protected String clientId;
    @XmlElement(nillable = true)
    protected ClientData clientData;

    /**
     * Ruft den Wert der args0-Eigenschaft ab.
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
     * Legt den Wert der args0-Eigenschaft fest.
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
     * Ruft den Wert der args1-Eigenschaft ab.
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
     * Legt den Wert der args1-Eigenschaft fest.
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
