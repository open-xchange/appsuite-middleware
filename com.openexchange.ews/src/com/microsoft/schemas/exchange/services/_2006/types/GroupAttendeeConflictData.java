
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GroupAttendeeConflictData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GroupAttendeeConflictData">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}AttendeeConflictData">
 *       &lt;sequence>
 *         &lt;element name="NumberOfMembers" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NumberOfMembersAvailable" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NumberOfMembersWithConflict" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NumberOfMembersWithNoData" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GroupAttendeeConflictData", propOrder = {
    "numberOfMembers",
    "numberOfMembersAvailable",
    "numberOfMembersWithConflict",
    "numberOfMembersWithNoData"
})
public class GroupAttendeeConflictData
    extends AttendeeConflictData
{

    @XmlElement(name = "NumberOfMembers")
    protected int numberOfMembers;
    @XmlElement(name = "NumberOfMembersAvailable")
    protected int numberOfMembersAvailable;
    @XmlElement(name = "NumberOfMembersWithConflict")
    protected int numberOfMembersWithConflict;
    @XmlElement(name = "NumberOfMembersWithNoData")
    protected int numberOfMembersWithNoData;

    /**
     * Gets the value of the numberOfMembers property.
     * 
     */
    public int getNumberOfMembers() {
        return numberOfMembers;
    }

    /**
     * Sets the value of the numberOfMembers property.
     * 
     */
    public void setNumberOfMembers(int value) {
        this.numberOfMembers = value;
    }

    /**
     * Gets the value of the numberOfMembersAvailable property.
     * 
     */
    public int getNumberOfMembersAvailable() {
        return numberOfMembersAvailable;
    }

    /**
     * Sets the value of the numberOfMembersAvailable property.
     * 
     */
    public void setNumberOfMembersAvailable(int value) {
        this.numberOfMembersAvailable = value;
    }

    /**
     * Gets the value of the numberOfMembersWithConflict property.
     * 
     */
    public int getNumberOfMembersWithConflict() {
        return numberOfMembersWithConflict;
    }

    /**
     * Sets the value of the numberOfMembersWithConflict property.
     * 
     */
    public void setNumberOfMembersWithConflict(int value) {
        this.numberOfMembersWithConflict = value;
    }

    /**
     * Gets the value of the numberOfMembersWithNoData property.
     * 
     */
    public int getNumberOfMembersWithNoData() {
        return numberOfMembersWithNoData;
    }

    /**
     * Sets the value of the numberOfMembersWithNoData property.
     * 
     */
    public void setNumberOfMembersWithNoData(int value) {
        this.numberOfMembersWithNoData = value;
    }

}
