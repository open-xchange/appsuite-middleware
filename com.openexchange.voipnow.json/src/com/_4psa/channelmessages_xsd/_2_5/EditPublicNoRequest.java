
package com._4psa.channelmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channeldata_xsd._2_5.PublicNoInfo;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ChannelData.xsd/2.5.1}PublicNoInfo">
 *       &lt;sequence>
 *         &lt;element name="didID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
    "didID"
})
@XmlRootElement(name = "EditPublicNoRequest")
public class EditPublicNoRequest
    extends PublicNoInfo
{

    @XmlElement(required = true)
    protected BigInteger didID;

    /**
     * Gets the value of the didID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getDidID() {
        return didID;
    }

    /**
     * Sets the value of the didID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setDidID(BigInteger value) {
        this.didID = value;
    }

}
