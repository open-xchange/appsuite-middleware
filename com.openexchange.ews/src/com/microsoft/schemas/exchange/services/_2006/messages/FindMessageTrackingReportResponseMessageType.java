
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfArraysOfTrackingPropertiesType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfFindMessageTrackingSearchResultType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfStringsType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfTrackingPropertiesType;


/**
 * <p>Java class for FindMessageTrackingReportResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindMessageTrackingReportResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="Diagnostics" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="MessageTrackingSearchResults" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfFindMessageTrackingSearchResultType" minOccurs="0"/>
 *         &lt;element name="ExecutedSearchScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Errors" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfArraysOfTrackingPropertiesType" minOccurs="0"/>
 *         &lt;element name="Properties" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfTrackingPropertiesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindMessageTrackingReportResponseMessageType", propOrder = {
    "diagnostics",
    "messageTrackingSearchResults",
    "executedSearchScope",
    "errors",
    "properties"
})
public class FindMessageTrackingReportResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "Diagnostics")
    protected ArrayOfStringsType diagnostics;
    @XmlElement(name = "MessageTrackingSearchResults")
    protected ArrayOfFindMessageTrackingSearchResultType messageTrackingSearchResults;
    @XmlElement(name = "ExecutedSearchScope")
    protected String executedSearchScope;
    @XmlElement(name = "Errors")
    protected ArrayOfArraysOfTrackingPropertiesType errors;
    @XmlElement(name = "Properties")
    protected ArrayOfTrackingPropertiesType properties;

    /**
     * Gets the value of the diagnostics property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getDiagnostics() {
        return diagnostics;
    }

    /**
     * Sets the value of the diagnostics property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setDiagnostics(ArrayOfStringsType value) {
        this.diagnostics = value;
    }

    /**
     * Gets the value of the messageTrackingSearchResults property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFindMessageTrackingSearchResultType }
     *     
     */
    public ArrayOfFindMessageTrackingSearchResultType getMessageTrackingSearchResults() {
        return messageTrackingSearchResults;
    }

    /**
     * Sets the value of the messageTrackingSearchResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFindMessageTrackingSearchResultType }
     *     
     */
    public void setMessageTrackingSearchResults(ArrayOfFindMessageTrackingSearchResultType value) {
        this.messageTrackingSearchResults = value;
    }

    /**
     * Gets the value of the executedSearchScope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutedSearchScope() {
        return executedSearchScope;
    }

    /**
     * Sets the value of the executedSearchScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutedSearchScope(String value) {
        this.executedSearchScope = value;
    }

    /**
     * Gets the value of the errors property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfArraysOfTrackingPropertiesType }
     *     
     */
    public ArrayOfArraysOfTrackingPropertiesType getErrors() {
        return errors;
    }

    /**
     * Sets the value of the errors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfArraysOfTrackingPropertiesType }
     *     
     */
    public void setErrors(ArrayOfArraysOfTrackingPropertiesType value) {
        this.errors = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTrackingPropertiesType }
     *     
     */
    public ArrayOfTrackingPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTrackingPropertiesType }
     *     
     */
    public void setProperties(ArrayOfTrackingPropertiesType value) {
        this.properties = value;
    }

}
