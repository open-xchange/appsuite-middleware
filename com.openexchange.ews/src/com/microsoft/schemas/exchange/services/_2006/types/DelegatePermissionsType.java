
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DelegatePermissionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DelegatePermissionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CalendarFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *         &lt;element name="TasksFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *         &lt;element name="InboxFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *         &lt;element name="ContactsFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *         &lt;element name="NotesFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *         &lt;element name="JournalFolderPermissionLevel" type="{http://schemas.microsoft.com/exchange/services/2006/types}DelegateFolderPermissionLevelType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelegatePermissionsType", propOrder = {
    "calendarFolderPermissionLevel",
    "tasksFolderPermissionLevel",
    "inboxFolderPermissionLevel",
    "contactsFolderPermissionLevel",
    "notesFolderPermissionLevel",
    "journalFolderPermissionLevel"
})
public class DelegatePermissionsType {

    @XmlElement(name = "CalendarFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType calendarFolderPermissionLevel;
    @XmlElement(name = "TasksFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType tasksFolderPermissionLevel;
    @XmlElement(name = "InboxFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType inboxFolderPermissionLevel;
    @XmlElement(name = "ContactsFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType contactsFolderPermissionLevel;
    @XmlElement(name = "NotesFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType notesFolderPermissionLevel;
    @XmlElement(name = "JournalFolderPermissionLevel")
    protected DelegateFolderPermissionLevelType journalFolderPermissionLevel;

    /**
     * Gets the value of the calendarFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getCalendarFolderPermissionLevel() {
        return calendarFolderPermissionLevel;
    }

    /**
     * Sets the value of the calendarFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setCalendarFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.calendarFolderPermissionLevel = value;
    }

    /**
     * Gets the value of the tasksFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getTasksFolderPermissionLevel() {
        return tasksFolderPermissionLevel;
    }

    /**
     * Sets the value of the tasksFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setTasksFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.tasksFolderPermissionLevel = value;
    }

    /**
     * Gets the value of the inboxFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getInboxFolderPermissionLevel() {
        return inboxFolderPermissionLevel;
    }

    /**
     * Sets the value of the inboxFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setInboxFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.inboxFolderPermissionLevel = value;
    }

    /**
     * Gets the value of the contactsFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getContactsFolderPermissionLevel() {
        return contactsFolderPermissionLevel;
    }

    /**
     * Sets the value of the contactsFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setContactsFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.contactsFolderPermissionLevel = value;
    }

    /**
     * Gets the value of the notesFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getNotesFolderPermissionLevel() {
        return notesFolderPermissionLevel;
    }

    /**
     * Sets the value of the notesFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setNotesFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.notesFolderPermissionLevel = value;
    }

    /**
     * Gets the value of the journalFolderPermissionLevel property.
     * 
     * @return
     *     possible object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public DelegateFolderPermissionLevelType getJournalFolderPermissionLevel() {
        return journalFolderPermissionLevel;
    }

    /**
     * Sets the value of the journalFolderPermissionLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelegateFolderPermissionLevelType }
     *     
     */
    public void setJournalFolderPermissionLevel(DelegateFolderPermissionLevelType value) {
        this.journalFolderPermissionLevel = value;
    }

}
