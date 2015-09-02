
package com.openexchange.push.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr PushUserClient complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PushUserClient">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="client" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pushUser" type="{http://soap.push.openexchange.com}PushUser" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PushUserClient", propOrder = {
    "client",
    "pushUser"
})
public class PushUserClient {

    @XmlElement(nillable = true)
    protected String client;
    @XmlElement(nillable = true)
    protected PushUser pushUser;

    /**
     * Ruft den Wert der client-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClient() {
        return client;
    }

    /**
     * Legt den Wert der client-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClient(String value) {
        this.client = value;
    }

    /**
     * Ruft den Wert der pushUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PushUser }
     *     
     */
    public PushUser getPushUser() {
        return pushUser;
    }

    /**
     * Legt den Wert der pushUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PushUser }
     *     
     */
    public void setPushUser(PushUser value) {
        this.pushUser = value;
    }

}
