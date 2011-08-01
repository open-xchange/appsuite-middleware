
package com._4psa.channelmessages_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channeldata_xsd._2_5.SIPChannelInfo;


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
 *         &lt;element name="type" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="sip"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element name="SIP" type="{http://4psa.com/ChannelData.xsd/2.5.1}SIPChannelInfo" minOccurs="0"/>
 *           &lt;element name="Dahdi" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/>
 *           &lt;element name="IAX" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/>
 *         &lt;/choice>
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
    "type",
    "sip"
})
@XmlRootElement(name = "AddChannelRequest")
public class AddChannelRequest {

    @XmlElement(defaultValue = "sip")
    protected String type;
    @XmlElement(name = "SIP")
    protected SIPChannelInfo sip;

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the sip property.
     *
     * @return
     *     possible object is
     *     {@link SIPChannelInfo }
     *
     */
    public SIPChannelInfo getSIP() {
        return sip;
    }

    /**
     * Sets the value of the sip property.
     *
     * @param value
     *     allowed object is
     *     {@link SIPChannelInfo }
     *
     */
    public void setSIP(SIPChannelInfo value) {
        this.sip = value;
    }

}
