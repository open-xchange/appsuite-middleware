
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfRulesType;


/**
 * <p>Java class for GetInboxRulesResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetInboxRulesResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="OutlookRuleBlobExists" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="InboxRules" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRulesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetInboxRulesResponseType", propOrder = {
    "outlookRuleBlobExists",
    "inboxRules"
})
public class GetInboxRulesResponseType
    extends ResponseMessageType
{

    @XmlElement(name = "OutlookRuleBlobExists")
    protected Boolean outlookRuleBlobExists;
    @XmlElement(name = "InboxRules")
    protected ArrayOfRulesType inboxRules;

    /**
     * Gets the value of the outlookRuleBlobExists property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOutlookRuleBlobExists() {
        return outlookRuleBlobExists;
    }

    /**
     * Sets the value of the outlookRuleBlobExists property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutlookRuleBlobExists(Boolean value) {
        this.outlookRuleBlobExists = value;
    }

    /**
     * Gets the value of the inboxRules property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRulesType }
     *     
     */
    public ArrayOfRulesType getInboxRules() {
        return inboxRules;
    }

    /**
     * Sets the value of the inboxRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRulesType }
     *     
     */
    public void setInboxRules(ArrayOfRulesType value) {
        this.inboxRules = value;
    }

}
