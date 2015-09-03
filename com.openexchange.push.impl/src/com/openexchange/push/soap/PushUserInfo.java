
package com.openexchange.push.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr PushUserInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PushUserInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="permanent" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
@XmlType(name = "PushUserInfo", propOrder = {
    "permanent",
    "pushUser"
})
public class PushUserInfo {

    protected Boolean permanent;
    @XmlElement(nillable = true)
    protected PushUser pushUser;

    /**
     * Ruft den Wert der permanent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPermanent() {
        return permanent;
    }

    /**
     * Legt den Wert der permanent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPermanent(Boolean value) {
        this.permanent = value;
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
