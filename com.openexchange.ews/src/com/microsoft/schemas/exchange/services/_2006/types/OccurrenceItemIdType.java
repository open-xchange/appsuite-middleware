
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OccurrenceItemIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OccurrenceItemIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseItemIdType">
 *       &lt;attribute name="RecurringMasterId" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}DerivedItemIdType" />
 *       &lt;attribute name="ChangeKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="InstanceIndex" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OccurrenceItemIdType")
public class OccurrenceItemIdType
    extends BaseItemIdType
{

    @XmlAttribute(name = "RecurringMasterId", required = true)
    protected String recurringMasterId;
    @XmlAttribute(name = "ChangeKey")
    protected String changeKey;
    @XmlAttribute(name = "InstanceIndex", required = true)
    protected int instanceIndex;

    /**
     * Gets the value of the recurringMasterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecurringMasterId() {
        return recurringMasterId;
    }

    /**
     * Sets the value of the recurringMasterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecurringMasterId(String value) {
        this.recurringMasterId = value;
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

    /**
     * Gets the value of the instanceIndex property.
     * 
     */
    public int getInstanceIndex() {
        return instanceIndex;
    }

    /**
     * Sets the value of the instanceIndex property.
     * 
     */
    public void setInstanceIndex(int value) {
        this.instanceIndex = value;
    }

}
