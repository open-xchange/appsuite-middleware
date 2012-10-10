
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeleteRuleOperationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteRuleOperationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RuleOperationType">
 *       &lt;sequence>
 *         &lt;element name="RuleId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteRuleOperationType", propOrder = {
    "ruleId"
})
public class DeleteRuleOperationType
    extends RuleOperationType
{

    @XmlElement(name = "RuleId", required = true)
    protected String ruleId;

    /**
     * Gets the value of the ruleId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * Sets the value of the ruleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuleId(String value) {
        this.ruleId = value;
    }

}
