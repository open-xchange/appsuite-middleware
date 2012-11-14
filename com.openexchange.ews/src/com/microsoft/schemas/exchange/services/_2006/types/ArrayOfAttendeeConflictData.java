
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfAttendeeConflictData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfAttendeeConflictData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="UnknownAttendeeConflictData" type="{http://schemas.microsoft.com/exchange/services/2006/types}UnknownAttendeeConflictData"/>
 *         &lt;element name="IndividualAttendeeConflictData" type="{http://schemas.microsoft.com/exchange/services/2006/types}IndividualAttendeeConflictData"/>
 *         &lt;element name="TooBigGroupAttendeeConflictData" type="{http://schemas.microsoft.com/exchange/services/2006/types}TooBigGroupAttendeeConflictData"/>
 *         &lt;element name="GroupAttendeeConflictData" type="{http://schemas.microsoft.com/exchange/services/2006/types}GroupAttendeeConflictData"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfAttendeeConflictData", propOrder = {
    "unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData"
})
public class ArrayOfAttendeeConflictData {

    @XmlElements({
        @XmlElement(name = "UnknownAttendeeConflictData", type = UnknownAttendeeConflictData.class, nillable = true),
        @XmlElement(name = "GroupAttendeeConflictData", type = GroupAttendeeConflictData.class, nillable = true),
        @XmlElement(name = "IndividualAttendeeConflictData", type = IndividualAttendeeConflictData.class, nillable = true),
        @XmlElement(name = "TooBigGroupAttendeeConflictData", type = TooBigGroupAttendeeConflictData.class, nillable = true)
    })
    protected List<AttendeeConflictData> unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData;

    /**
     * Gets the value of the unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UnknownAttendeeConflictData }
     * {@link GroupAttendeeConflictData }
     * {@link IndividualAttendeeConflictData }
     * {@link TooBigGroupAttendeeConflictData }
     * 
     * 
     */
    public List<AttendeeConflictData> getUnknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData() {
        if (unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData == null) {
            unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData = new ArrayList<AttendeeConflictData>();
        }
        return this.unknownAttendeeConflictDataOrIndividualAttendeeConflictDataOrTooBigGroupAttendeeConflictData;
    }

}
