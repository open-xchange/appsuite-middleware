
package com.openexchange.admin.soap.user.dataobjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UserModuleAccess complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UserModuleAccess">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OLOX20" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="USM" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="activeSync" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="calendar" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="collectEmailAddresses" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="contacts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="delegateTask" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="deniedPortal" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="editGroup" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="editPassword" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="editPublicFolders" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="editResource" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="forum" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="globalAddressBookDisabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ical" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="infostore" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="multipleMailAccounts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="pinboardWrite" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="projects" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="publicFolderEditable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="publication" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="readCreateSharedFolders" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="rssBookmarks" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="rssPortal" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="subscription" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="syncml" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="tasks" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="vcard" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="webdav" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="webdavXml" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="webmail" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserModuleAccess", propOrder = {
    "olox20",
    "usm",
    "activeSync",
    "calendar",
    "collectEmailAddresses",
    "contacts",
    "delegateTask",
    "deniedPortal",
    "editGroup",
    "editPassword",
    "editPublicFolders",
    "editResource",
    "forum",
    "globalAddressBookDisabled",
    "ical",
    "infostore",
    "multipleMailAccounts",
    "pinboardWrite",
    "projects",
    "publicFolderEditable",
    "publication",
    "readCreateSharedFolders",
    "rssBookmarks",
    "rssPortal",
    "subscription",
    "syncml",
    "tasks",
    "vcard",
    "webdav",
    "webdavXml",
    "webmail"
})
public class UserModuleAccess {

    @XmlElement(name = "OLOX20", nillable = true)
    protected Boolean olox20;
    @XmlElement(name = "USM", nillable = true)
    protected Boolean usm;
    @XmlElement(nillable = true)
    protected Boolean activeSync;
    @XmlElement(nillable = true)
    protected Boolean calendar;
    @XmlElement(nillable = true)
    protected Boolean collectEmailAddresses;
    @XmlElement(nillable = true)
    protected Boolean contacts;
    @XmlElement(nillable = true)
    protected Boolean delegateTask;
    @XmlElement(nillable = true)
    protected Boolean deniedPortal;
    @XmlElement(nillable = true)
    protected Boolean editGroup;
    @XmlElement(nillable = true)
    protected Boolean editPassword;
    @XmlElement(nillable = true)
    protected Boolean editPublicFolders;
    @XmlElement(nillable = true)
    protected Boolean editResource;
    @XmlElement(nillable = true)
    protected Boolean forum;
    @XmlElement(nillable = true)
    protected Boolean globalAddressBookDisabled;
    @XmlElement(nillable = true)
    protected Boolean ical;
    @XmlElement(nillable = true)
    protected Boolean infostore;
    @XmlElement(nillable = true)
    protected Boolean multipleMailAccounts;
    @XmlElement(nillable = true)
    protected Boolean pinboardWrite;
    @XmlElement(nillable = true)
    protected Boolean projects;
    @XmlElement(nillable = true)
    protected Boolean publicFolderEditable;
    @XmlElement(nillable = true)
    protected Boolean publication;
    @XmlElement(nillable = true)
    protected Boolean readCreateSharedFolders;
    @XmlElement(nillable = true)
    protected Boolean rssBookmarks;
    @XmlElement(nillable = true)
    protected Boolean rssPortal;
    @XmlElement(nillable = true)
    protected Boolean subscription;
    @XmlElement(nillable = true)
    protected Boolean syncml;
    @XmlElement(nillable = true)
    protected Boolean tasks;
    @XmlElement(nillable = true)
    protected Boolean vcard;
    @XmlElement(nillable = true)
    protected Boolean webdav;
    @XmlElement(nillable = true)
    protected Boolean webdavXml;
    @XmlElement(nillable = true)
    protected Boolean webmail;

    /**
     * Ruft den Wert der olox20-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOLOX20() {
        return olox20;
    }

    /**
     * Legt den Wert der olox20-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOLOX20(Boolean value) {
        this.olox20 = value;
    }

    /**
     * Ruft den Wert der usm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUSM() {
        return usm;
    }

    /**
     * Legt den Wert der usm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUSM(Boolean value) {
        this.usm = value;
    }

    /**
     * Ruft den Wert der activeSync-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isActiveSync() {
        return activeSync;
    }

    /**
     * Legt den Wert der activeSync-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActiveSync(Boolean value) {
        this.activeSync = value;
    }

    /**
     * Ruft den Wert der calendar-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCalendar() {
        return calendar;
    }

    /**
     * Legt den Wert der calendar-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCalendar(Boolean value) {
        this.calendar = value;
    }

    /**
     * Ruft den Wert der collectEmailAddresses-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCollectEmailAddresses() {
        return collectEmailAddresses;
    }

    /**
     * Legt den Wert der collectEmailAddresses-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCollectEmailAddresses(Boolean value) {
        this.collectEmailAddresses = value;
    }

    /**
     * Ruft den Wert der contacts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isContacts() {
        return contacts;
    }

    /**
     * Legt den Wert der contacts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContacts(Boolean value) {
        this.contacts = value;
    }

    /**
     * Ruft den Wert der delegateTask-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDelegateTask() {
        return delegateTask;
    }

    /**
     * Legt den Wert der delegateTask-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelegateTask(Boolean value) {
        this.delegateTask = value;
    }

    /**
     * Ruft den Wert der deniedPortal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeniedPortal() {
        return deniedPortal;
    }

    /**
     * Legt den Wert der deniedPortal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeniedPortal(Boolean value) {
        this.deniedPortal = value;
    }

    /**
     * Ruft den Wert der editGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEditGroup() {
        return editGroup;
    }

    /**
     * Legt den Wert der editGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEditGroup(Boolean value) {
        this.editGroup = value;
    }

    /**
     * Ruft den Wert der editPassword-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEditPassword() {
        return editPassword;
    }

    /**
     * Legt den Wert der editPassword-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEditPassword(Boolean value) {
        this.editPassword = value;
    }

    /**
     * Ruft den Wert der editPublicFolders-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEditPublicFolders() {
        return editPublicFolders;
    }

    /**
     * Legt den Wert der editPublicFolders-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEditPublicFolders(Boolean value) {
        this.editPublicFolders = value;
    }

    /**
     * Ruft den Wert der editResource-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEditResource() {
        return editResource;
    }

    /**
     * Legt den Wert der editResource-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEditResource(Boolean value) {
        this.editResource = value;
    }

    /**
     * Ruft den Wert der forum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForum() {
        return forum;
    }

    /**
     * Legt den Wert der forum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForum(Boolean value) {
        this.forum = value;
    }

    /**
     * Ruft den Wert der globalAddressBookDisabled-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isGlobalAddressBookDisabled() {
        return globalAddressBookDisabled;
    }

    /**
     * Legt den Wert der globalAddressBookDisabled-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGlobalAddressBookDisabled(Boolean value) {
        this.globalAddressBookDisabled = value;
    }

    /**
     * Ruft den Wert der ical-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIcal() {
        return ical;
    }

    /**
     * Legt den Wert der ical-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIcal(Boolean value) {
        this.ical = value;
    }

    /**
     * Ruft den Wert der infostore-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInfostore() {
        return infostore;
    }

    /**
     * Legt den Wert der infostore-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInfostore(Boolean value) {
        this.infostore = value;
    }

    /**
     * Ruft den Wert der multipleMailAccounts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMultipleMailAccounts() {
        return multipleMailAccounts;
    }

    /**
     * Legt den Wert der multipleMailAccounts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultipleMailAccounts(Boolean value) {
        this.multipleMailAccounts = value;
    }

    /**
     * Ruft den Wert der pinboardWrite-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPinboardWrite() {
        return pinboardWrite;
    }

    /**
     * Legt den Wert der pinboardWrite-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPinboardWrite(Boolean value) {
        this.pinboardWrite = value;
    }

    /**
     * Ruft den Wert der projects-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isProjects() {
        return projects;
    }

    /**
     * Legt den Wert der projects-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProjects(Boolean value) {
        this.projects = value;
    }

    /**
     * Ruft den Wert der publicFolderEditable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPublicFolderEditable() {
        return publicFolderEditable;
    }

    /**
     * Legt den Wert der publicFolderEditable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublicFolderEditable(Boolean value) {
        this.publicFolderEditable = value;
    }

    /**
     * Ruft den Wert der publication-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPublication() {
        return publication;
    }

    /**
     * Legt den Wert der publication-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublication(Boolean value) {
        this.publication = value;
    }

    /**
     * Ruft den Wert der readCreateSharedFolders-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    /**
     * Legt den Wert der readCreateSharedFolders-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReadCreateSharedFolders(Boolean value) {
        this.readCreateSharedFolders = value;
    }

    /**
     * Ruft den Wert der rssBookmarks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRssBookmarks() {
        return rssBookmarks;
    }

    /**
     * Legt den Wert der rssBookmarks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRssBookmarks(Boolean value) {
        this.rssBookmarks = value;
    }

    /**
     * Ruft den Wert der rssPortal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRssPortal() {
        return rssPortal;
    }

    /**
     * Legt den Wert der rssPortal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRssPortal(Boolean value) {
        this.rssPortal = value;
    }

    /**
     * Ruft den Wert der subscription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSubscription() {
        return subscription;
    }

    /**
     * Legt den Wert der subscription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubscription(Boolean value) {
        this.subscription = value;
    }

    /**
     * Ruft den Wert der syncml-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSyncml() {
        return syncml;
    }

    /**
     * Legt den Wert der syncml-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSyncml(Boolean value) {
        this.syncml = value;
    }

    /**
     * Ruft den Wert der tasks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTasks() {
        return tasks;
    }

    /**
     * Legt den Wert der tasks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTasks(Boolean value) {
        this.tasks = value;
    }

    /**
     * Ruft den Wert der vcard-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVcard() {
        return vcard;
    }

    /**
     * Legt den Wert der vcard-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVcard(Boolean value) {
        this.vcard = value;
    }

    /**
     * Ruft den Wert der webdav-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWebdav() {
        return webdav;
    }

    /**
     * Legt den Wert der webdav-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWebdav(Boolean value) {
        this.webdav = value;
    }

    /**
     * Ruft den Wert der webdavXml-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWebdavXml() {
        return webdavXml;
    }

    /**
     * Legt den Wert der webdavXml-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWebdavXml(Boolean value) {
        this.webdavXml = value;
    }

    /**
     * Ruft den Wert der webmail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWebmail() {
        return webmail;
    }

    /**
     * Legt den Wert der webmail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWebmail(Boolean value) {
        this.webmail = value;
    }

}
