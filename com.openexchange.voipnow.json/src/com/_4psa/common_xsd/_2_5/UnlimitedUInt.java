
package com._4psa.common_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * unlimited attribute on integer type
 *
 * <p>Java class for unlimitedUInt complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="unlimitedUInt">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>unsignedInt">
 *       &lt;attribute name="unlimited" type="{http://4psa.com/Common.xsd/2.5.1}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "unlimitedUInt", propOrder = {
    "value"
})
public class UnlimitedUInt {

    @XmlValue
    @XmlSchemaType(name = "unsignedInt")
    protected long value;
    @XmlAttribute(name = "unlimited")
    protected Boolean unlimited;

    /**
     * Gets the value of the value property.
     *
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Gets the value of the unlimited property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isUnlimited() {
        if (unlimited == null) {
            return true;
        } else {
            return unlimited;
        }
    }

    /**
     * Sets the value of the unlimited property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUnlimited(Boolean value) {
        this.unlimited = value;
    }

}
