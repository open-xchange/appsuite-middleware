
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SetFolderFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SetFolderFieldType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}FolderChangeDescriptionType">
 *       &lt;choice>
 *         &lt;element name="Folder" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderType"/>
 *         &lt;element name="CalendarFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarFolderType"/>
 *         &lt;element name="ContactsFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContactsFolderType"/>
 *         &lt;element name="SearchFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}SearchFolderType"/>
 *         &lt;element name="TasksFolder" type="{http://schemas.microsoft.com/exchange/services/2006/types}TasksFolderType"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SetFolderFieldType", propOrder = {
    "folder",
    "calendarFolder",
    "contactsFolder",
    "searchFolder",
    "tasksFolder"
})
public class SetFolderFieldType
    extends FolderChangeDescriptionType
{

    @XmlElement(name = "Folder")
    protected FolderType folder;
    @XmlElement(name = "CalendarFolder")
    protected CalendarFolderType calendarFolder;
    @XmlElement(name = "ContactsFolder")
    protected ContactsFolderType contactsFolder;
    @XmlElement(name = "SearchFolder")
    protected SearchFolderType searchFolder;
    @XmlElement(name = "TasksFolder")
    protected TasksFolderType tasksFolder;

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link FolderType }
     *     
     */
    public FolderType getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderType }
     *     
     */
    public void setFolder(FolderType value) {
        this.folder = value;
    }

    /**
     * Gets the value of the calendarFolder property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarFolderType }
     *     
     */
    public CalendarFolderType getCalendarFolder() {
        return calendarFolder;
    }

    /**
     * Sets the value of the calendarFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarFolderType }
     *     
     */
    public void setCalendarFolder(CalendarFolderType value) {
        this.calendarFolder = value;
    }

    /**
     * Gets the value of the contactsFolder property.
     * 
     * @return
     *     possible object is
     *     {@link ContactsFolderType }
     *     
     */
    public ContactsFolderType getContactsFolder() {
        return contactsFolder;
    }

    /**
     * Sets the value of the contactsFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactsFolderType }
     *     
     */
    public void setContactsFolder(ContactsFolderType value) {
        this.contactsFolder = value;
    }

    /**
     * Gets the value of the searchFolder property.
     * 
     * @return
     *     possible object is
     *     {@link SearchFolderType }
     *     
     */
    public SearchFolderType getSearchFolder() {
        return searchFolder;
    }

    /**
     * Sets the value of the searchFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchFolderType }
     *     
     */
    public void setSearchFolder(SearchFolderType value) {
        this.searchFolder = value;
    }

    /**
     * Gets the value of the tasksFolder property.
     * 
     * @return
     *     possible object is
     *     {@link TasksFolderType }
     *     
     */
    public TasksFolderType getTasksFolder() {
        return tasksFolder;
    }

    /**
     * Sets the value of the tasksFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link TasksFolderType }
     *     
     */
    public void setTasksFolder(TasksFolderType value) {
        this.tasksFolder = value;
    }

}
