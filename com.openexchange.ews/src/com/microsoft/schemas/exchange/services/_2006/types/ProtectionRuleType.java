
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProtectionRuleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProtectionRuleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Condition" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleConditionType"/>
 *         &lt;element name="Action" type="{http://schemas.microsoft.com/exchange/services/2006/types}ProtectionRuleActionType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Name" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;minLength value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="UserOverridable" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="Priority" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectionRuleType", propOrder = {
    "condition",
    "action"
})
public class ProtectionRuleType {

    @XmlElement(name = "Condition", required = true)
    protected ProtectionRuleConditionType condition;
    @XmlElement(name = "Action", required = true)
    protected ProtectionRuleActionType action;
    @XmlAttribute(name = "Name", required = true)
    protected String name;
    @XmlAttribute(name = "UserOverridable", required = true)
    protected boolean userOverridable;
    @XmlAttribute(name = "Priority", required = true)
    protected int priority;

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRuleConditionType }
     *     
     */
    public ProtectionRuleConditionType getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRuleConditionType }
     *     
     */
    public void setCondition(ProtectionRuleConditionType value) {
        this.condition = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link ProtectionRuleActionType }
     *     
     */
    public ProtectionRuleActionType getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProtectionRuleActionType }
     *     
     */
    public void setAction(ProtectionRuleActionType value) {
        this.action = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the userOverridable property.
     * 
     */
    public boolean isUserOverridable() {
        return userOverridable;
    }

    /**
     * Sets the value of the userOverridable property.
     * 
     */
    public void setUserOverridable(boolean value) {
        this.userOverridable = value;
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

}
