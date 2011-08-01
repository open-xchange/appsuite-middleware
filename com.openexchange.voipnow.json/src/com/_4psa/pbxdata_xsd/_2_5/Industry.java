
package com._4psa.pbxdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Industry data
 *
 * <p>Java class for Industry complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Industry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="standardZoneCode" type="{http://4psa.com/Common.xsd/2.5.1}text"/>
 *         &lt;element name="standardZoneHeight" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="extendedZoneCode" type="{http://4psa.com/Common.xsd/2.5.1}text" minOccurs="0"/>
 *         &lt;element name="extendedZoneHeight" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Industry", propOrder = {
    "id",
    "name",
    "standardZoneCode",
    "standardZoneHeight",
    "extendedZoneCode",
    "extendedZoneHeight"
})
public class Industry {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String standardZoneCode;
    @XmlElement(required = true, defaultValue = "100")
    protected BigInteger standardZoneHeight;
    protected String extendedZoneCode;
    @XmlElement(defaultValue = "450")
    protected BigInteger extendedZoneHeight;

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the standardZoneCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStandardZoneCode() {
        return standardZoneCode;
    }

    /**
     * Sets the value of the standardZoneCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStandardZoneCode(String value) {
        this.standardZoneCode = value;
    }

    /**
     * Gets the value of the standardZoneHeight property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getStandardZoneHeight() {
        return standardZoneHeight;
    }

    /**
     * Sets the value of the standardZoneHeight property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setStandardZoneHeight(BigInteger value) {
        this.standardZoneHeight = value;
    }

    /**
     * Gets the value of the extendedZoneCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtendedZoneCode() {
        return extendedZoneCode;
    }

    /**
     * Sets the value of the extendedZoneCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtendedZoneCode(String value) {
        this.extendedZoneCode = value;
    }

    /**
     * Gets the value of the extendedZoneHeight property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getExtendedZoneHeight() {
        return extendedZoneHeight;
    }

    /**
     * Sets the value of the extendedZoneHeight property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setExtendedZoneHeight(BigInteger value) {
        this.extendedZoneHeight = value;
    }

}
