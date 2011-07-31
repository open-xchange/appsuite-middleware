
package com._4psa.billingdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Charging plan recharge limits data
 *
 * <p>Java class for LimitsList complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="LimitsList">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}Limits">
 *       &lt;sequence>
 *         &lt;element name="limitID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LimitsList", propOrder = {
    "limitID",
    "crDate"
})
public class LimitsList
    extends Limits
{

    @XmlElement(required = true)
    protected BigInteger limitID;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar crDate;

    /**
     * Gets the value of the limitID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getLimitID() {
        return limitID;
    }

    /**
     * Sets the value of the limitID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setLimitID(BigInteger value) {
        this.limitID = value;
    }

    /**
     * Gets the value of the crDate property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getCrDate() {
        return crDate;
    }

    /**
     * Sets the value of the crDate property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setCrDate(XMLGregorianCalendar value) {
        this.crDate = value;
    }

}
