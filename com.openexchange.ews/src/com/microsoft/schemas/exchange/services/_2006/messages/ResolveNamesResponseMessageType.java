
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfResolutionType;


/**
 * <p>Java class for ResolveNamesResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResolveNamesResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="ResolutionSet" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfResolutionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResolveNamesResponseMessageType", propOrder = {
    "resolutionSet"
})
public class ResolveNamesResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "ResolutionSet")
    protected ArrayOfResolutionType resolutionSet;

    /**
     * Gets the value of the resolutionSet property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfResolutionType }
     *     
     */
    public ArrayOfResolutionType getResolutionSet() {
        return resolutionSet;
    }

    /**
     * Sets the value of the resolutionSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfResolutionType }
     *     
     */
    public void setResolutionSet(ArrayOfResolutionType value) {
        this.resolutionSet = value;
    }

}
