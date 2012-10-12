
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UnifiedMessageServiceConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnifiedMessageServiceConfiguration">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ServiceConfiguration">
 *       &lt;sequence>
 *         &lt;element name="UmEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="PlayOnPhoneDialString" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PlayOnPhoneEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnifiedMessageServiceConfiguration", propOrder = {
    "umEnabled",
    "playOnPhoneDialString",
    "playOnPhoneEnabled"
})
public class UnifiedMessageServiceConfiguration
    extends ServiceConfiguration
{

    @XmlElement(name = "UmEnabled")
    protected boolean umEnabled;
    @XmlElement(name = "PlayOnPhoneDialString", required = true)
    protected String playOnPhoneDialString;
    @XmlElement(name = "PlayOnPhoneEnabled")
    protected boolean playOnPhoneEnabled;

    /**
     * Gets the value of the umEnabled property.
     * 
     */
    public boolean isUmEnabled() {
        return umEnabled;
    }

    /**
     * Sets the value of the umEnabled property.
     * 
     */
    public void setUmEnabled(boolean value) {
        this.umEnabled = value;
    }

    /**
     * Gets the value of the playOnPhoneDialString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlayOnPhoneDialString() {
        return playOnPhoneDialString;
    }

    /**
     * Sets the value of the playOnPhoneDialString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlayOnPhoneDialString(String value) {
        this.playOnPhoneDialString = value;
    }

    /**
     * Gets the value of the playOnPhoneEnabled property.
     * 
     */
    public boolean isPlayOnPhoneEnabled() {
        return playOnPhoneEnabled;
    }

    /**
     * Sets the value of the playOnPhoneEnabled property.
     * 
     */
    public void setPlayOnPhoneEnabled(boolean value) {
        this.playOnPhoneEnabled = value;
    }

}
