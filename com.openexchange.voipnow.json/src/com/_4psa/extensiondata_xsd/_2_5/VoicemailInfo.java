
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Voicemail extension data
 *
 * <p>Java class for VoicemailInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="VoicemailInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="connectionSnd" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VoicemailInfo", propOrder = {
    "connectionSndActive",
    "connectionSnd"
})
public class VoicemailInfo {

    protected Boolean connectionSndActive;
    protected BigInteger connectionSnd;

    /**
     * Gets the value of the connectionSndActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isConnectionSndActive() {
        return connectionSndActive;
    }

    /**
     * Sets the value of the connectionSndActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setConnectionSndActive(Boolean value) {
        this.connectionSndActive = value;
    }

    /**
     * Gets the value of the connectionSnd property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getConnectionSnd() {
        return connectionSnd;
    }

    /**
     * Sets the value of the connectionSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setConnectionSnd(BigInteger value) {
        this.connectionSnd = value;
    }

}
