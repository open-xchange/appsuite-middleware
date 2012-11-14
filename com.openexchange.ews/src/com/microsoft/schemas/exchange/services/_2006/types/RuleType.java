
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Rule type
 * 
 * <p>Java class for RuleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RuleId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="IsEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsNotSupported" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsInError" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Conditions" type="{http://schemas.microsoft.com/exchange/services/2006/types}RulePredicatesType" minOccurs="0"/>
 *         &lt;element name="Exceptions" type="{http://schemas.microsoft.com/exchange/services/2006/types}RulePredicatesType" minOccurs="0"/>
 *         &lt;element name="Actions" type="{http://schemas.microsoft.com/exchange/services/2006/types}RuleActionsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleType", propOrder = {
    "ruleId",
    "displayName",
    "priority",
    "isEnabled",
    "isNotSupported",
    "isInError",
    "conditions",
    "exceptions",
    "actions"
})
public class RuleType {

    @XmlElement(name = "RuleId")
    protected String ruleId;
    @XmlElement(name = "DisplayName", required = true)
    protected String displayName;
    @XmlElement(name = "Priority")
    protected int priority;
    @XmlElement(name = "IsEnabled")
    protected boolean isEnabled;
    @XmlElement(name = "IsNotSupported")
    protected Boolean isNotSupported;
    @XmlElement(name = "IsInError")
    protected Boolean isInError;
    @XmlElement(name = "Conditions")
    protected RulePredicatesType conditions;
    @XmlElement(name = "Exceptions")
    protected RulePredicatesType exceptions;
    @XmlElement(name = "Actions")
    protected RuleActionsType actions;

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

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
    }

    /**
     * Gets the value of the isEnabled property.
     * 
     */
    public boolean isIsEnabled() {
        return isEnabled;
    }

    /**
     * Sets the value of the isEnabled property.
     * 
     */
    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
    }

    /**
     * Gets the value of the isNotSupported property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsNotSupported() {
        return isNotSupported;
    }

    /**
     * Sets the value of the isNotSupported property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsNotSupported(Boolean value) {
        this.isNotSupported = value;
    }

    /**
     * Gets the value of the isInError property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsInError() {
        return isInError;
    }

    /**
     * Sets the value of the isInError property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsInError(Boolean value) {
        this.isInError = value;
    }

    /**
     * Gets the value of the conditions property.
     * 
     * @return
     *     possible object is
     *     {@link RulePredicatesType }
     *     
     */
    public RulePredicatesType getConditions() {
        return conditions;
    }

    /**
     * Sets the value of the conditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link RulePredicatesType }
     *     
     */
    public void setConditions(RulePredicatesType value) {
        this.conditions = value;
    }

    /**
     * Gets the value of the exceptions property.
     * 
     * @return
     *     possible object is
     *     {@link RulePredicatesType }
     *     
     */
    public RulePredicatesType getExceptions() {
        return exceptions;
    }

    /**
     * Sets the value of the exceptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link RulePredicatesType }
     *     
     */
    public void setExceptions(RulePredicatesType value) {
        this.exceptions = value;
    }

    /**
     * Gets the value of the actions property.
     * 
     * @return
     *     possible object is
     *     {@link RuleActionsType }
     *     
     */
    public RuleActionsType getActions() {
        return actions;
    }

    /**
     * Sets the value of the actions property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleActionsType }
     *     
     */
    public void setActions(RuleActionsType value) {
        this.actions = value;
    }

}
