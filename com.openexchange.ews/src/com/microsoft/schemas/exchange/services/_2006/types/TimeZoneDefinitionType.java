
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TimeZoneDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimeZoneDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="Periods" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfPeriodsType"/>
 *         &lt;element name="TransitionsGroups" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTransitionsGroupsType" minOccurs="0"/>
 *         &lt;element name="Transitions" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTransitionsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeZoneDefinitionType", propOrder = {
    "periods",
    "transitionsGroups",
    "transitions"
})
public class TimeZoneDefinitionType {

    @XmlElement(name = "Periods")
    protected NonEmptyArrayOfPeriodsType periods;
    @XmlElement(name = "TransitionsGroups")
    protected ArrayOfTransitionsGroupsType transitionsGroups;
    @XmlElement(name = "Transitions")
    protected ArrayOfTransitionsType transitions;
    @XmlAttribute(name = "Id")
    protected String id;
    @XmlAttribute(name = "Name")
    protected String name;

    /**
     * Gets the value of the periods property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfPeriodsType }
     *     
     */
    public NonEmptyArrayOfPeriodsType getPeriods() {
        return periods;
    }

    /**
     * Sets the value of the periods property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfPeriodsType }
     *     
     */
    public void setPeriods(NonEmptyArrayOfPeriodsType value) {
        this.periods = value;
    }

    /**
     * Gets the value of the transitionsGroups property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTransitionsGroupsType }
     *     
     */
    public ArrayOfTransitionsGroupsType getTransitionsGroups() {
        return transitionsGroups;
    }

    /**
     * Sets the value of the transitionsGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTransitionsGroupsType }
     *     
     */
    public void setTransitionsGroups(ArrayOfTransitionsGroupsType value) {
        this.transitionsGroups = value;
    }

    /**
     * Gets the value of the transitions property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTransitionsType }
     *     
     */
    public ArrayOfTransitionsType getTransitions() {
        return transitions;
    }

    /**
     * Sets the value of the transitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTransitionsType }
     *     
     */
    public void setTransitions(ArrayOfTransitionsType value) {
        this.transitions = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
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

}
