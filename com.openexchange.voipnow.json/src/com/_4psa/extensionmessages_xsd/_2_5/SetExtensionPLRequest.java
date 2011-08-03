
package com._4psa.extensionmessages_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Share;
import com._4psa.extensiondata_xsd._2_5.ExtensionPLInfo;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtensionPLInfo">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="shareVoicemail" type="{http://4psa.com/Common.xsd/2.5.1}share" minOccurs="0"/>
 *         &lt;element name="shareFaxes" type="{http://4psa.com/Common.xsd/2.5.1}share" minOccurs="0"/>
 *         &lt;element name="shareRecordings" type="{http://4psa.com/Common.xsd/2.5.1}share" minOccurs="0"/>
 *         &lt;element name="shareCallHistory" type="{http://4psa.com/Common.xsd/2.5.1}share" minOccurs="0"/>
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
    "shareVoicemail",
    "shareFaxes",
    "shareRecordings",
    "shareCallHistory"
})
@XmlRootElement(name = "SetExtensionPLRequest")
public class SetExtensionPLRequest
    extends ExtensionPLInfo
{

    protected Share shareVoicemail;
    protected Share shareFaxes;
    protected Share shareRecordings;
    protected Share shareCallHistory;

    /**
     * Gets the value of the shareVoicemail property.
     *
     * @return
     *     possible object is
     *     {@link Share }
     *
     */
    public Share getShareVoicemail() {
        return shareVoicemail;
    }

    /**
     * Sets the value of the shareVoicemail property.
     *
     * @param value
     *     allowed object is
     *     {@link Share }
     *
     */
    public void setShareVoicemail(Share value) {
        this.shareVoicemail = value;
    }

    /**
     * Gets the value of the shareFaxes property.
     *
     * @return
     *     possible object is
     *     {@link Share }
     *
     */
    public Share getShareFaxes() {
        return shareFaxes;
    }

    /**
     * Sets the value of the shareFaxes property.
     *
     * @param value
     *     allowed object is
     *     {@link Share }
     *
     */
    public void setShareFaxes(Share value) {
        this.shareFaxes = value;
    }

    /**
     * Gets the value of the shareRecordings property.
     *
     * @return
     *     possible object is
     *     {@link Share }
     *
     */
    public Share getShareRecordings() {
        return shareRecordings;
    }

    /**
     * Sets the value of the shareRecordings property.
     *
     * @param value
     *     allowed object is
     *     {@link Share }
     *
     */
    public void setShareRecordings(Share value) {
        this.shareRecordings = value;
    }

    /**
     * Gets the value of the shareCallHistory property.
     *
     * @return
     *     possible object is
     *     {@link Share }
     *
     */
    public Share getShareCallHistory() {
        return shareCallHistory;
    }

    /**
     * Sets the value of the shareCallHistory property.
     *
     * @param value
     *     allowed object is
     *     {@link Share }
     *
     */
    public void setShareCallHistory(Share value) {
        this.shareCallHistory = value;
    }

}
