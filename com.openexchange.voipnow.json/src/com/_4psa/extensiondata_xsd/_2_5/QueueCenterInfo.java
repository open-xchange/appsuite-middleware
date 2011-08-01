
package com._4psa.extensiondata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Queue center login extension data
 *
 * <p>Java class for QueueCenterInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="QueueCenterInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneCallerIDInfo">
 *       &lt;sequence>
 *         &lt;element name="connectionSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="connectionSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueueCenterInfo", propOrder = {
    "connectionSnd",
    "connectionSndActive"
})
public class QueueCenterInfo
    extends PhoneCallerIDInfo
{

    protected String connectionSnd;
    protected Boolean connectionSndActive;

    /**
     * Gets the value of the connectionSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getConnectionSnd() {
        return connectionSnd;
    }

    /**
     * Sets the value of the connectionSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setConnectionSnd(String value) {
        this.connectionSnd = value;
    }

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

}
