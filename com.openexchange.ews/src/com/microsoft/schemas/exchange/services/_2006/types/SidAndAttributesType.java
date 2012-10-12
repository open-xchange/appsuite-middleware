
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SidAndAttributesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SidAndAttributesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SecurityIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Attributes" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SidAndAttributesType", propOrder = {
    "securityIdentifier"
})
public class SidAndAttributesType {

    @XmlElement(name = "SecurityIdentifier", required = true)
    protected String securityIdentifier;
    @XmlAttribute(name = "Attributes", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long attributes;

    /**
     * Gets the value of the securityIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityIdentifier() {
        return securityIdentifier;
    }

    /**
     * Sets the value of the securityIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityIdentifier(String value) {
        this.securityIdentifier = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     */
    public long getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     */
    public void setAttributes(long value) {
        this.attributes = value;
    }

}
