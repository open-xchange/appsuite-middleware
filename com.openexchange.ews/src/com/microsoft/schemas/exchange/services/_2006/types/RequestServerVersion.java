
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Version" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}ExchangeVersionType" fixed="Exchange2010_SP2" />
 *       &lt;anyAttribute namespace='http://schemas.xmlsoap.org/soap/envelope/'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "RequestServerVersion")
public class RequestServerVersion {

    @XmlAttribute(name = "Version", required = true)
    protected ExchangeVersionType version;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link ExchangeVersionType }
     *     
     */
    public ExchangeVersionType getVersion() {
        if (version == null) {
            return ExchangeVersionType.EXCHANGE_2010_SP_2;
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExchangeVersionType }
     *     
     */
    public void setVersion(ExchangeVersionType value) {
        this.version = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
