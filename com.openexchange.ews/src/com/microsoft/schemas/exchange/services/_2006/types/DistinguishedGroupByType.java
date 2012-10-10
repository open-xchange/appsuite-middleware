
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Allows consumers to access standard groupings for FindItem queries.  This is in
 *         contrast to the arbitrary (custom) groupings available via the t:GroupByType
 *       
 * 
 * <p>Java class for DistinguishedGroupByType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistinguishedGroupByType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseGroupByType">
 *       &lt;sequence>
 *         &lt;element name="StandardGroupBy" type="{http://schemas.microsoft.com/exchange/services/2006/types}StandardGroupByType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistinguishedGroupByType", propOrder = {
    "standardGroupBy"
})
public class DistinguishedGroupByType
    extends BaseGroupByType
{

    @XmlElement(name = "StandardGroupBy", required = true)
    protected StandardGroupByType standardGroupBy;

    /**
     * Gets the value of the standardGroupBy property.
     * 
     * @return
     *     possible object is
     *     {@link StandardGroupByType }
     *     
     */
    public StandardGroupByType getStandardGroupBy() {
        return standardGroupBy;
    }

    /**
     * Sets the value of the standardGroupBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardGroupByType }
     *     
     */
    public void setStandardGroupBy(StandardGroupByType value) {
        this.standardGroupBy = value;
    }

}
