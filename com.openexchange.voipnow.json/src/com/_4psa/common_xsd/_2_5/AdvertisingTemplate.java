
package com._4psa.common_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * Advertisig template used for client accounts.
 * 
 * <p>Java class for advertisingTemplate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="advertisingTemplate">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="GlobalDefault" type="{http://4psa.com/Common.xsd/2.5.1}boolean" default="false" />
 *       &lt;attribute name="ResellerDefault" type="{http://4psa.com/Common.xsd/2.5.1}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "advertisingTemplate", propOrder = {
    "value"
})
public class AdvertisingTemplate {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "GlobalDefault")
    protected Boolean globalDefault;
    @XmlAttribute(name = "ResellerDefault")
    protected Boolean resellerDefault;

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
     * Gets the value of the globalDefault property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGlobalDefault() {
        if (globalDefault == null) {
            return false;
        } else {
            return globalDefault;
        }
    }

    /**
     * Sets the value of the globalDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGlobalDefault(Boolean value) {
        this.globalDefault = value;
    }

    /**
     * Gets the value of the resellerDefault property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isResellerDefault() {
        if (resellerDefault == null) {
            return false;
        } else {
            return resellerDefault;
        }
    }

    /**
     * Sets the value of the resellerDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setResellerDefault(Boolean value) {
        this.resellerDefault = value;
    }

}
