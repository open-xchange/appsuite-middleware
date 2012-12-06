
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IndividualAttendeeConflictData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IndividualAttendeeConflictData">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}AttendeeConflictData">
 *       &lt;sequence>
 *         &lt;element name="BusyType" type="{http://schemas.microsoft.com/exchange/services/2006/types}LegacyFreeBusyType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IndividualAttendeeConflictData", propOrder = {
    "busyType"
})
public class IndividualAttendeeConflictData
    extends AttendeeConflictData
{

    @XmlElement(name = "BusyType", required = true)
    protected LegacyFreeBusyType busyType;

    /**
     * Gets the value of the busyType property.
     * 
     * @return
     *     possible object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public LegacyFreeBusyType getBusyType() {
        return busyType;
    }

    /**
     * Sets the value of the busyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegacyFreeBusyType }
     *     
     */
    public void setBusyType(LegacyFreeBusyType value) {
        this.busyType = value;
    }

}
