
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.ClientInfo;
import com._4psa.extensionmessages_xsd._2_5.AddExtensionRequest;
import com._4psa.extensionmessages_xsd._2_5.EditExtensionRequest;


/**
 * Extension data
 *
 * <p>Java class for ExtensionInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtensionInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ClientInfo">
 *       &lt;sequence>
 *         &lt;element name="phoneLang" type="{http://4psa.com/Common.xsd/2.5.1}code" minOccurs="0"/>
 *         &lt;element name="channelRuleId" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionInfo", propOrder = {
    "phoneLang",
    "channelRuleId"
})
@XmlSeeAlso({
    AddExtensionRequest.class,
    ExtendedExtensionInfo.class,
    EditExtensionRequest.class
})
public class ExtensionInfo
    extends ClientInfo
{

    protected String phoneLang;
    protected BigInteger channelRuleId;

    /**
     * Gets the value of the phoneLang property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhoneLang() {
        return phoneLang;
    }

    /**
     * Sets the value of the phoneLang property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhoneLang(String value) {
        this.phoneLang = value;
    }

    /**
     * Gets the value of the channelRuleId property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChannelRuleId() {
        return channelRuleId;
    }

    /**
     * Sets the value of the channelRuleId property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChannelRuleId(BigInteger value) {
        this.channelRuleId = value;
    }

}
