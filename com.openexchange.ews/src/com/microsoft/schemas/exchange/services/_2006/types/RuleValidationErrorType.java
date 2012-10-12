
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a single validation error on a particular rule property value, predicate property value or action property value
 * 
 * <p>Java class for RuleValidationErrorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleValidationErrorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}RuleFieldURIType"/>
 *         &lt;element name="ErrorCode" type="{http://schemas.microsoft.com/exchange/services/2006/types}RuleValidationErrorCodeType"/>
 *         &lt;element name="ErrorMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FieldValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleValidationErrorType", propOrder = {
    "fieldURI",
    "errorCode",
    "errorMessage",
    "fieldValue"
})
public class RuleValidationErrorType {

    @XmlElement(name = "FieldURI", required = true)
    protected RuleFieldURIType fieldURI;
    @XmlElement(name = "ErrorCode", required = true)
    protected RuleValidationErrorCodeType errorCode;
    @XmlElement(name = "ErrorMessage", required = true)
    protected String errorMessage;
    @XmlElement(name = "FieldValue", required = true)
    protected String fieldValue;

    /**
     * Gets the value of the fieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link RuleFieldURIType }
     *     
     */
    public RuleFieldURIType getFieldURI() {
        return fieldURI;
    }

    /**
     * Sets the value of the fieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleFieldURIType }
     *     
     */
    public void setFieldURI(RuleFieldURIType value) {
        this.fieldURI = value;
    }

    /**
     * Gets the value of the errorCode property.
     * 
     * @return
     *     possible object is
     *     {@link RuleValidationErrorCodeType }
     *     
     */
    public RuleValidationErrorCodeType getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuleValidationErrorCodeType }
     *     
     */
    public void setErrorCode(RuleValidationErrorCodeType value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the fieldValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldValue() {
        return fieldValue;
    }

    /**
     * Sets the value of the fieldValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldValue(String value) {
        this.fieldValue = value;
    }

}
