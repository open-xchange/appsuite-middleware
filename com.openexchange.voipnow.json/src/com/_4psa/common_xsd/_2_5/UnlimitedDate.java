
package com._4psa.common_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Unlimited attribute on date type
 *
 * <p>Java class for unlimitedDate complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="unlimitedDate">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>date">
 *       &lt;attribute name="unlimited" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "unlimitedDate", propOrder = {
    "value"
})
public class UnlimitedDate {

    @XmlValue
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar value;
    @XmlAttribute(name = "unlimited")
    protected Boolean unlimited;

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setValue(XMLGregorianCalendar value) {
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
