
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Represents an extended property instance (both its path identifier along with its
 *         associated value).
 *       
 * 
 * <p>Java class for ExtendedPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtendedPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExtendedFieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToExtendedFieldType"/>
 *         &lt;choice>
 *           &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="Values" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfPropertyValuesType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendedPropertyType", propOrder = {
    "extendedFieldURI",
    "value",
    "values"
})
public class ExtendedPropertyType {

    @XmlElement(name = "ExtendedFieldURI", required = true)
    protected PathToExtendedFieldType extendedFieldURI;
    @XmlElement(name = "Value")
    protected String value;
    @XmlElement(name = "Values")
    protected NonEmptyArrayOfPropertyValuesType values;

    /**
     * Gets the value of the extendedFieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link PathToExtendedFieldType }
     *     
     */
    public PathToExtendedFieldType getExtendedFieldURI() {
        return extendedFieldURI;
    }

    /**
     * Sets the value of the extendedFieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToExtendedFieldType }
     *     
     */
    public void setExtendedFieldURI(PathToExtendedFieldType value) {
        this.extendedFieldURI = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the values property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfPropertyValuesType }
     *     
     */
    public NonEmptyArrayOfPropertyValuesType getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfPropertyValuesType }
     *     
     */
    public void setValues(NonEmptyArrayOfPropertyValuesType value) {
        this.values = value;
    }

}
