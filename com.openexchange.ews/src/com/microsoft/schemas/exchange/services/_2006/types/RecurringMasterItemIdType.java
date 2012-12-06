
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecurringMasterItemIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecurringMasterItemIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseItemIdType">
 *       &lt;attribute name="OccurrenceId" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}DerivedItemIdType" />
 *       &lt;attribute name="ChangeKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecurringMasterItemIdType")
public class RecurringMasterItemIdType
    extends BaseItemIdType
{

    @XmlAttribute(name = "OccurrenceId", required = true)
    protected String occurrenceId;
    @XmlAttribute(name = "ChangeKey")
    protected String changeKey;

    /**
     * Gets the value of the occurrenceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOccurrenceId() {
        return occurrenceId;
    }

    /**
     * Sets the value of the occurrenceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOccurrenceId(String value) {
        this.occurrenceId = value;
    }

    /**
     * Gets the value of the changeKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeKey() {
        return changeKey;
    }

    /**
     * Sets the value of the changeKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeKey(String value) {
        this.changeKey = value;
    }

}
