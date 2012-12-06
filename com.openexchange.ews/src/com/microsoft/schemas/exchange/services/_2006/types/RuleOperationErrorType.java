
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RuleOperationErrorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleOperationErrorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OperationIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ValidationErrors" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRuleValidationErrorsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleOperationErrorType", propOrder = {
    "operationIndex",
    "validationErrors"
})
public class RuleOperationErrorType {

    @XmlElement(name = "OperationIndex")
    protected int operationIndex;
    @XmlElement(name = "ValidationErrors", required = true)
    protected ArrayOfRuleValidationErrorsType validationErrors;

    /**
     * Gets the value of the operationIndex property.
     * 
     */
    public int getOperationIndex() {
        return operationIndex;
    }

    /**
     * Sets the value of the operationIndex property.
     * 
     */
    public void setOperationIndex(int value) {
        this.operationIndex = value;
    }

    /**
     * Gets the value of the validationErrors property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRuleValidationErrorsType }
     *     
     */
    public ArrayOfRuleValidationErrorsType getValidationErrors() {
        return validationErrors;
    }

    /**
     * Sets the value of the validationErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRuleValidationErrorsType }
     *     
     */
    public void setValidationErrors(ArrayOfRuleValidationErrorsType value) {
        this.validationErrors = value;
    }

}
