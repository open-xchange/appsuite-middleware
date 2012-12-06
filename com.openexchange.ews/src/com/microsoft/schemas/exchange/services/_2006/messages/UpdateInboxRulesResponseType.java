
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfRuleOperationErrorsType;


/**
 * <p>Java class for UpdateInboxRulesResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateInboxRulesResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="RuleOperationErrors" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRuleOperationErrorsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateInboxRulesResponseType", propOrder = {
    "ruleOperationErrors"
})
public class UpdateInboxRulesResponseType
    extends ResponseMessageType
{

    @XmlElement(name = "RuleOperationErrors")
    protected ArrayOfRuleOperationErrorsType ruleOperationErrors;

    /**
     * Gets the value of the ruleOperationErrors property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRuleOperationErrorsType }
     *     
     */
    public ArrayOfRuleOperationErrorsType getRuleOperationErrors() {
        return ruleOperationErrors;
    }

    /**
     * Sets the value of the ruleOperationErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRuleOperationErrorsType }
     *     
     */
    public void setRuleOperationErrors(ArrayOfRuleOperationErrorsType value) {
        this.ruleOperationErrors = value;
    }

}
