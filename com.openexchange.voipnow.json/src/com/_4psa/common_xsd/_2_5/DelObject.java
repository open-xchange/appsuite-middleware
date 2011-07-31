
package com._4psa.common_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelCallRulesOutGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelChannelGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelChannelResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.DelPublicNoResponseType;
import com._4psa.clientmessagesinfo_xsd._2_5.MoveClientsResponseType;


/**
 * Delete operation response object type
 *
 * <p>Java class for delObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="delObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="items" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="result" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="success"/>
 *               &lt;enumeration value="partial"/>
 *               &lt;enumeration value="failure"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delObject", propOrder = {
    "items",
    "result"
})
@XmlSeeAlso({
    DelChannelResponseType.class,
    DelPublicNoResponseType.class,
    DelCallRulesOutGroupResponseType.class,
    DelChannelGroupResponseType.class,
    MoveClientsResponseType.class
})
public class DelObject {

    protected BigInteger items;
    protected String result;

    /**
     * Gets the value of the items property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setItems(BigInteger value) {
        this.items = value;
    }

    /**
     * Gets the value of the result property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setResult(String value) {
        this.result = value;
    }

}
