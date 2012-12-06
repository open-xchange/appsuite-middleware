
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfArraysOfTrackingPropertiesType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfStringsType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfTrackingPropertiesType;
import com.microsoft.schemas.exchange.services._2006.types.MessageTrackingReportType;


/**
 * <p>Java class for GetMessageTrackingReportResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetMessageTrackingReportResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="MessageTrackingReport" type="{http://schemas.microsoft.com/exchange/services/2006/types}MessageTrackingReportType" minOccurs="0"/>
 *         &lt;element name="Diagnostics" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
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
@XmlType(name = "GetMessageTrackingReportResponseMessageType", propOrder = {
    "messageTrackingReport",
    "diagnostics",
    "errors",
    "properties"
})
public class GetMessageTrackingReportResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "MessageTrackingReport")
    protected MessageTrackingReportType messageTrackingReport;
    @XmlElement(name = "Diagnostics")
    protected ArrayOfStringsType diagnostics;
    @XmlElement(name = "Errors")
    protected ArrayOfArraysOfTrackingPropertiesType errors;
    @XmlElement(name = "Properties")
    protected ArrayOfTrackingPropertiesType properties;

    /**
     * Gets the value of the messageTrackingReport property.
     * 
     * @return
     *     possible object is
     *     {@link MessageTrackingReportType }
     *     
     */
    public MessageTrackingReportType getMessageTrackingReport() {
        return messageTrackingReport;
    }

    /**
     * Sets the value of the messageTrackingReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageTrackingReportType }
     *     
     */
    public void setMessageTrackingReport(MessageTrackingReportType value) {
        this.messageTrackingReport = value;
    }

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
