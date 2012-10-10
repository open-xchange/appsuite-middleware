
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProtectionRulesServiceConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProtectionRulesServiceConfiguration">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ServiceConfiguration">
 *       &lt;sequence>
 *         &lt;element name="Rules" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfProtectionRulesType"/>
 *         &lt;element name="InternalDomains" type="{http://schemas.microsoft.com/exchange/services/2006/types}SmtpDomainList"/>
 *       &lt;/sequence>
 *       &lt;attribute name="RefreshInterval" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProtectionRulesServiceConfiguration", propOrder = {
    "rules",
    "internalDomains"
})
public class ProtectionRulesServiceConfiguration
    extends ServiceConfiguration
{

    @XmlElement(name = "Rules", required = true)
    protected ArrayOfProtectionRulesType rules;
    @XmlElement(name = "InternalDomains", required = true)
    protected SmtpDomainList internalDomains;
    @XmlAttribute(name = "RefreshInterval", required = true)
    protected int refreshInterval;

    /**
     * Gets the value of the rules property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfProtectionRulesType }
     *     
     */
    public ArrayOfProtectionRulesType getRules() {
        return rules;
    }

    /**
     * Sets the value of the rules property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfProtectionRulesType }
     *     
     */
    public void setRules(ArrayOfProtectionRulesType value) {
        this.rules = value;
    }

    /**
     * Gets the value of the internalDomains property.
     * 
     * @return
     *     possible object is
     *     {@link SmtpDomainList }
     *     
     */
    public SmtpDomainList getInternalDomains() {
        return internalDomains;
    }

    /**
     * Sets the value of the internalDomains property.
     * 
     * @param value
     *     allowed object is
     *     {@link SmtpDomainList }
     *     
     */
    public void setInternalDomains(SmtpDomainList value) {
        this.internalDomains = value;
    }

    /**
     * Gets the value of the refreshInterval property.
     * 
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Sets the value of the refreshInterval property.
     * 
     */
    public void setRefreshInterval(int value) {
        this.refreshInterval = value;
    }

}
