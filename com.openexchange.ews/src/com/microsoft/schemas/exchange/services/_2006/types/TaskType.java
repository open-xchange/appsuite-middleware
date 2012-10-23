
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for TaskType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="ActualWork" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="AssignedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="BillingInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ChangeCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Companies" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="CompleteDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Contacts" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="DelegationState" type="{http://schemas.microsoft.com/exchange/services/2006/types}TaskDelegateStateType" minOccurs="0"/>
 *         &lt;element name="Delegator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="IsAssignmentEditable" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="IsComplete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsRecurring" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsTeamTask" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Mileage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Owner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PercentComplete" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="Recurrence" type="{http://schemas.microsoft.com/exchange/services/2006/types}TaskRecurrenceType" minOccurs="0"/>
 *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://schemas.microsoft.com/exchange/services/2006/types}TaskStatusType" minOccurs="0"/>
 *         &lt;element name="StatusDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TotalWork" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskType", propOrder = {
    "actualWork",
    "assignedTime",
    "billingInformation",
    "changeCount",
    "companies",
    "completeDate",
    "contacts",
    "delegationState",
    "delegator",
    "dueDate",
    "isAssignmentEditable",
    "isComplete",
    "isRecurring",
    "isTeamTask",
    "mileage",
    "owner",
    "percentComplete",
    "recurrence",
    "startDate",
    "status",
    "statusDescription",
    "totalWork"
})
public class TaskType
    extends ItemType
{

    @XmlElement(name = "ActualWork")
    protected Integer actualWork;
    @XmlElement(name = "AssignedTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar assignedTime;
    @XmlElement(name = "BillingInformation")
    protected String billingInformation;
    @XmlElement(name = "ChangeCount")
    protected Integer changeCount;
    @XmlElement(name = "Companies")
    protected ArrayOfStringsType companies;
    @XmlElement(name = "CompleteDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar completeDate;
    @XmlElement(name = "Contacts")
    protected ArrayOfStringsType contacts;
    @XmlElement(name = "DelegationState")
    protected TaskDelegateStateType delegationState;
    @XmlElement(name = "Delegator")
    protected String delegator;
    @XmlElement(name = "DueDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dueDate;
    @XmlElement(name = "IsAssignmentEditable")
    protected Integer isAssignmentEditable;
    @XmlElement(name = "IsComplete")
    protected Boolean isComplete;
    @XmlElement(name = "IsRecurring")
    protected Boolean isRecurring;
    @XmlElement(name = "IsTeamTask")
    protected Boolean isTeamTask;
    @XmlElement(name = "Mileage")
    protected String mileage;
    @XmlElement(name = "Owner")
    protected String owner;
    @XmlElement(name = "PercentComplete")
    protected Double percentComplete;
    @XmlElement(name = "Recurrence")
    protected TaskRecurrenceType recurrence;
    @XmlElement(name = "StartDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlElement(name = "Status")
    protected TaskStatusType status;
    @XmlElement(name = "StatusDescription")
    protected String statusDescription;
    @XmlElement(name = "TotalWork")
    protected Integer totalWork;

    /**
     * Gets the value of the actualWork property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getActualWork() {
        return actualWork;
    }

    /**
     * Sets the value of the actualWork property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setActualWork(Integer value) {
        this.actualWork = value;
    }

    /**
     * Gets the value of the assignedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAssignedTime() {
        return assignedTime;
    }

    /**
     * Sets the value of the assignedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAssignedTime(XMLGregorianCalendar value) {
        this.assignedTime = value;
    }

    /**
     * Gets the value of the billingInformation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBillingInformation() {
        return billingInformation;
    }

    /**
     * Sets the value of the billingInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBillingInformation(String value) {
        this.billingInformation = value;
    }

    /**
     * Gets the value of the changeCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getChangeCount() {
        return changeCount;
    }

    /**
     * Sets the value of the changeCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setChangeCount(Integer value) {
        this.changeCount = value;
    }

    /**
     * Gets the value of the companies property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getCompanies() {
        return companies;
    }

    /**
     * Sets the value of the companies property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setCompanies(ArrayOfStringsType value) {
        this.companies = value;
    }

    /**
     * Gets the value of the completeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCompleteDate() {
        return completeDate;
    }

    /**
     * Sets the value of the completeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCompleteDate(XMLGregorianCalendar value) {
        this.completeDate = value;
    }

    /**
     * Gets the value of the contacts property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getContacts() {
        return contacts;
    }

    /**
     * Sets the value of the contacts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setContacts(ArrayOfStringsType value) {
        this.contacts = value;
    }

    /**
     * Gets the value of the delegationState property.
     * 
     * @return
     *     possible object is
     *     {@link TaskDelegateStateType }
     *     
     */
    public TaskDelegateStateType getDelegationState() {
        return delegationState;
    }

    /**
     * Sets the value of the delegationState property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskDelegateStateType }
     *     
     */
    public void setDelegationState(TaskDelegateStateType value) {
        this.delegationState = value;
    }

    /**
     * Gets the value of the delegator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelegator() {
        return delegator;
    }

    /**
     * Sets the value of the delegator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelegator(String value) {
        this.delegator = value;
    }

    /**
     * Gets the value of the dueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDueDate() {
        return dueDate;
    }

    /**
     * Sets the value of the dueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDueDate(XMLGregorianCalendar value) {
        this.dueDate = value;
    }

    /**
     * Gets the value of the isAssignmentEditable property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIsAssignmentEditable() {
        return isAssignmentEditable;
    }

    /**
     * Sets the value of the isAssignmentEditable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIsAssignmentEditable(Integer value) {
        this.isAssignmentEditable = value;
    }

    /**
     * Gets the value of the isComplete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsComplete() {
        return isComplete;
    }

    /**
     * Sets the value of the isComplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsComplete(Boolean value) {
        this.isComplete = value;
    }

    /**
     * Gets the value of the isRecurring property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRecurring() {
        return isRecurring;
    }

    /**
     * Sets the value of the isRecurring property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRecurring(Boolean value) {
        this.isRecurring = value;
    }

    /**
     * Gets the value of the isTeamTask property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsTeamTask() {
        return isTeamTask;
    }

    /**
     * Sets the value of the isTeamTask property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsTeamTask(Boolean value) {
        this.isTeamTask = value;
    }

    /**
     * Gets the value of the mileage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMileage() {
        return mileage;
    }

    /**
     * Sets the value of the mileage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMileage(String value) {
        this.mileage = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the percentComplete property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPercentComplete() {
        return percentComplete;
    }

    /**
     * Sets the value of the percentComplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPercentComplete(Double value) {
        this.percentComplete = value;
    }

    /**
     * Gets the value of the recurrence property.
     * 
     * @return
     *     possible object is
     *     {@link TaskRecurrenceType }
     *     
     */
    public TaskRecurrenceType getRecurrence() {
        return recurrence;
    }

    /**
     * Sets the value of the recurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskRecurrenceType }
     *     
     */
    public void setRecurrence(TaskRecurrenceType value) {
        this.recurrence = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link TaskStatusType }
     *     
     */
    public TaskStatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskStatusType }
     *     
     */
    public void setStatus(TaskStatusType value) {
        this.status = value;
    }

    /**
     * Gets the value of the statusDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * Sets the value of the statusDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDescription(String value) {
        this.statusDescription = value;
    }

    /**
     * Gets the value of the totalWork property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalWork() {
        return totalWork;
    }

    /**
     * Sets the value of the totalWork property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalWork(Integer value) {
        this.totalWork = value;
    }

}
