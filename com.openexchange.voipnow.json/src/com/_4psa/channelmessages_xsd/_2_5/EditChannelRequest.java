
package com._4psa.channelmessages_xsd._2_5;

import java.math.BigInteger;
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
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
    "id",
    "sip"
})
@XmlRootElement(name = "EditChannelRequest")
public class EditChannelRequest {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    @XmlElement(name = "SIP")
    protected SIPChannelInfo sip;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setID(BigInteger value) {
        this.id = value;
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
