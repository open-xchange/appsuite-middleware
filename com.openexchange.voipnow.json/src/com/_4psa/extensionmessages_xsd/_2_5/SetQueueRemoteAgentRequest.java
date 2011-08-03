
package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.RemoteAgent;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}RemoteAgent">
 *       &lt;sequence>
 *         &lt;element name="queueID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="queueIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="PIN" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "queueID",
    "queueIdentifier",
    "auth",
    "pin"
})
@XmlRootElement(name = "SetQueueRemoteAgentRequest")
public class SetQueueRemoteAgentRequest
    extends RemoteAgent
{

    protected BigInteger queueID;
    protected String queueIdentifier;
    @XmlElement(defaultValue = "false")
    protected Boolean auth;
    @XmlElement(name = "PIN")
    protected String pin;

    /**
     * Gets the value of the queueID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getQueueID() {
        return queueID;
    }

    /**
     * Sets the value of the queueID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setQueueID(BigInteger value) {
        this.queueID = value;
    }

    /**
     * Gets the value of the queueIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getQueueIdentifier() {
        return queueIdentifier;
    }

    /**
     * Sets the value of the queueIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setQueueIdentifier(String value) {
        this.queueIdentifier = value;
    }

    /**
     * Gets the value of the auth property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAuth() {
        return auth;
    }

    /**
     * Sets the value of the auth property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAuth(Boolean value) {
        this.auth = value;
    }

    /**
     * Gets the value of the pin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPIN() {
        return pin;
    }

    /**
     * Sets the value of the pin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPIN(String value) {
        this.pin = value;
    }

}
