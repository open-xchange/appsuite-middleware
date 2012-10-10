
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProtectionRuleConditionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProtectionRuleConditionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="AllInternal" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleAllInternalType"/>
 *         &lt;element name="And" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleAndType"/>
 *         &lt;element name="RecipientIs" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleRecipientIsType"/>
 *         &lt;element name="SenderDepartments" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleSenderDepartmentsType"/>
 *         &lt;element name="True" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleTrueType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectionRuleConditionType", propOrder = {
    "allInternal",
    "and",
    "recipientIs",
    "senderDepartments",
    "_true"
})
public class ProtectionRuleConditionType {

    @XmlElement(name = "AllInternal")
    protected String allInternal;
    @XmlElement(name = "And")
    protected ProtectionRuleAndType and;
    @XmlElement(name = "RecipientIs")
    protected ProtectionRuleRecipientIsType recipientIs;
    @XmlElement(name = "SenderDepartments")
    protected ProtectionRuleSenderDepartmentsType senderDepartments;
    @XmlElement(name = "True")
    protected String _true;

    /**
     * Gets the value of the allInternal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllInternal() {
        return allInternal;
    }

    /**
     * Sets the value of the allInternal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllInternal(String value) {
        this.allInternal = value;
    }

    /**
     * Gets the value of the and property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRuleAndType }
     *     
     */
    public ProtectionRuleAndType getAnd() {
        return and;
    }

    /**
     * Sets the value of the and property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRuleAndType }
     *     
     */
    public void setAnd(ProtectionRuleAndType value) {
        this.and = value;
    }

    /**
     * Gets the value of the recipientIs property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRuleRecipientIsType }
     *     
     */
    public ProtectionRuleRecipientIsType getRecipientIs() {
        return recipientIs;
    }

    /**
     * Sets the value of the recipientIs property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRuleRecipientIsType }
     *     
     */
    public void setRecipientIs(ProtectionRuleRecipientIsType value) {
        this.recipientIs = value;
    }

    /**
     * Gets the value of the senderDepartments property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRuleSenderDepartmentsType }
     *     
     */
    public ProtectionRuleSenderDepartmentsType getSenderDepartments() {
        return senderDepartments;
    }

    /**
     * Sets the value of the senderDepartments property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRuleSenderDepartmentsType }
     *     
     */
    public void setSenderDepartments(ProtectionRuleSenderDepartmentsType value) {
        this.senderDepartments = value;
    }

    /**
     * Gets the value of the true property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrue() {
        return _true;
    }

    /**
     * Sets the value of the true property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrue(String value) {
        this._true = value;
    }

}
