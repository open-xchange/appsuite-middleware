
package com._4psa.common_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Disk space data
 * 
 * <p>Java class for diskSpace complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="diskSpace">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="used" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "diskSpace", propOrder = {
    "used",
    "total"
})
public class DiskSpace {

    protected String used;
    protected UnlimitedUString total;

    /**
     * Gets the value of the used property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsed() {
        return used;
    }

    /**
     * Sets the value of the used property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsed(String value) {
        this.used = value;
    }

    /**
     * Gets the value of the total property.
     * 
     * @return
     *     possible object is
     *     {@link UnlimitedUString }
     *     
     */
    public UnlimitedUString getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnlimitedUString }
     *     
     */
    public void setTotal(UnlimitedUString value) {
        this.total = value;
    }

}
