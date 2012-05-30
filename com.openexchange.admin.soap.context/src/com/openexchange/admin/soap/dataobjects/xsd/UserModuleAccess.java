
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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

    @XmlElementRef(name = "OLOX20", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> olox20;
    @XmlElementRef(name = "USM", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> usm;
    @XmlElementRef(name = "activeSync", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> activeSync;
    @XmlElementRef(name = "calendar", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> calendar;
    @XmlElementRef(name = "collectEmailAddresses", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> collectEmailAddresses;
    @XmlElementRef(name = "contacts", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> contacts;
    @XmlElementRef(name = "delegateTask", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> delegateTask;
    @XmlElementRef(name = "deniedPortal", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> deniedPortal;
    @XmlElementRef(name = "editGroup", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> editGroup;
    @XmlElementRef(name = "editPassword", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> editPassword;
    @XmlElementRef(name = "editPublicFolders", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> editPublicFolders;
    @XmlElementRef(name = "editResource", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> editResource;
    @XmlElementRef(name = "forum", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> forum;
    @XmlElementRef(name = "globalAddressBookDisabled", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> globalAddressBookDisabled;
    @XmlElementRef(name = "ical", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> ical;
    @XmlElementRef(name = "infostore", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> infostore;
    @XmlElementRef(name = "multipleMailAccounts", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> multipleMailAccounts;
    @XmlElementRef(name = "pinboardWrite", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> pinboardWrite;
    @XmlElementRef(name = "projects", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> projects;
    @XmlElementRef(name = "publicFolderEditable", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> publicFolderEditable;
    @XmlElementRef(name = "publication", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> publication;
    @XmlElementRef(name = "readCreateSharedFolders", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> readCreateSharedFolders;
    @XmlElementRef(name = "rssBookmarks", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> rssBookmarks;
    @XmlElementRef(name = "rssPortal", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> rssPortal;
    @XmlElementRef(name = "subscription", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> subscription;
    @XmlElementRef(name = "syncml", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> syncml;
    @XmlElementRef(name = "tasks", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> tasks;
    @XmlElementRef(name = "vcard", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> vcard;
    @XmlElementRef(name = "webdav", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> webdav;
    @XmlElementRef(name = "webdavXml", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> webdavXml;
    @XmlElementRef(name = "webmail", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> webmail;

    /**
     * Ruft den Wert der olox20-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getOLOX20() {
        return olox20;
    }

    /**
     * Legt den Wert der olox20-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setOLOX20(JAXBElement<Boolean> value) {
        this.olox20 = value;
    }

    /**
     * Ruft den Wert der usm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getUSM() {
        return usm;
    }

    /**
     * Legt den Wert der usm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setUSM(JAXBElement<Boolean> value) {
        this.usm = value;
    }

    /**
     * Ruft den Wert der activeSync-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getActiveSync() {
        return activeSync;
    }

    /**
     * Legt den Wert der activeSync-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setActiveSync(JAXBElement<Boolean> value) {
        this.activeSync = value;
    }

    /**
     * Ruft den Wert der calendar-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getCalendar() {
        return calendar;
    }

    /**
     * Legt den Wert der calendar-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setCalendar(JAXBElement<Boolean> value) {
        this.calendar = value;
    }

    /**
     * Ruft den Wert der collectEmailAddresses-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getCollectEmailAddresses() {
        return collectEmailAddresses;
    }

    /**
     * Legt den Wert der collectEmailAddresses-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setCollectEmailAddresses(JAXBElement<Boolean> value) {
        this.collectEmailAddresses = value;
    }

    /**
     * Ruft den Wert der contacts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getContacts() {
        return contacts;
    }

    /**
     * Legt den Wert der contacts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setContacts(JAXBElement<Boolean> value) {
        this.contacts = value;
    }

    /**
     * Ruft den Wert der delegateTask-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getDelegateTask() {
        return delegateTask;
    }

    /**
     * Legt den Wert der delegateTask-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setDelegateTask(JAXBElement<Boolean> value) {
        this.delegateTask = value;
    }

    /**
     * Ruft den Wert der deniedPortal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getDeniedPortal() {
        return deniedPortal;
    }

    /**
     * Legt den Wert der deniedPortal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setDeniedPortal(JAXBElement<Boolean> value) {
        this.deniedPortal = value;
    }

    /**
     * Ruft den Wert der editGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getEditGroup() {
        return editGroup;
    }

    /**
     * Legt den Wert der editGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setEditGroup(JAXBElement<Boolean> value) {
        this.editGroup = value;
    }

    /**
     * Ruft den Wert der editPassword-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getEditPassword() {
        return editPassword;
    }

    /**
     * Legt den Wert der editPassword-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setEditPassword(JAXBElement<Boolean> value) {
        this.editPassword = value;
    }

    /**
     * Ruft den Wert der editPublicFolders-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getEditPublicFolders() {
        return editPublicFolders;
    }

    /**
     * Legt den Wert der editPublicFolders-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setEditPublicFolders(JAXBElement<Boolean> value) {
        this.editPublicFolders = value;
    }

    /**
     * Ruft den Wert der editResource-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getEditResource() {
        return editResource;
    }

    /**
     * Legt den Wert der editResource-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setEditResource(JAXBElement<Boolean> value) {
        this.editResource = value;
    }

    /**
     * Ruft den Wert der forum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getForum() {
        return forum;
    }

    /**
     * Legt den Wert der forum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setForum(JAXBElement<Boolean> value) {
        this.forum = value;
    }

    /**
     * Ruft den Wert der globalAddressBookDisabled-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getGlobalAddressBookDisabled() {
        return globalAddressBookDisabled;
    }

    /**
     * Legt den Wert der globalAddressBookDisabled-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setGlobalAddressBookDisabled(JAXBElement<Boolean> value) {
        this.globalAddressBookDisabled = value;
    }

    /**
     * Ruft den Wert der ical-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIcal() {
        return ical;
    }

    /**
     * Legt den Wert der ical-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIcal(JAXBElement<Boolean> value) {
        this.ical = value;
    }

    /**
     * Ruft den Wert der infostore-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getInfostore() {
        return infostore;
    }

    /**
     * Legt den Wert der infostore-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setInfostore(JAXBElement<Boolean> value) {
        this.infostore = value;
    }

    /**
     * Ruft den Wert der multipleMailAccounts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMultipleMailAccounts() {
        return multipleMailAccounts;
    }

    /**
     * Legt den Wert der multipleMailAccounts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMultipleMailAccounts(JAXBElement<Boolean> value) {
        this.multipleMailAccounts = value;
    }

    /**
     * Ruft den Wert der pinboardWrite-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getPinboardWrite() {
        return pinboardWrite;
    }

    /**
     * Legt den Wert der pinboardWrite-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setPinboardWrite(JAXBElement<Boolean> value) {
        this.pinboardWrite = value;
    }

    /**
     * Ruft den Wert der projects-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getProjects() {
        return projects;
    }

    /**
     * Legt den Wert der projects-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setProjects(JAXBElement<Boolean> value) {
        this.projects = value;
    }

    /**
     * Ruft den Wert der publicFolderEditable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getPublicFolderEditable() {
        return publicFolderEditable;
    }

    /**
     * Legt den Wert der publicFolderEditable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setPublicFolderEditable(JAXBElement<Boolean> value) {
        this.publicFolderEditable = value;
    }

    /**
     * Ruft den Wert der publication-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getPublication() {
        return publication;
    }

    /**
     * Legt den Wert der publication-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setPublication(JAXBElement<Boolean> value) {
        this.publication = value;
    }

    /**
     * Ruft den Wert der readCreateSharedFolders-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    /**
     * Legt den Wert der readCreateSharedFolders-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setReadCreateSharedFolders(JAXBElement<Boolean> value) {
        this.readCreateSharedFolders = value;
    }

    /**
     * Ruft den Wert der rssBookmarks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getRssBookmarks() {
        return rssBookmarks;
    }

    /**
     * Legt den Wert der rssBookmarks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setRssBookmarks(JAXBElement<Boolean> value) {
        this.rssBookmarks = value;
    }

    /**
     * Ruft den Wert der rssPortal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getRssPortal() {
        return rssPortal;
    }

    /**
     * Legt den Wert der rssPortal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setRssPortal(JAXBElement<Boolean> value) {
        this.rssPortal = value;
    }

    /**
     * Ruft den Wert der subscription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getSubscription() {
        return subscription;
    }

    /**
     * Legt den Wert der subscription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setSubscription(JAXBElement<Boolean> value) {
        this.subscription = value;
    }

    /**
     * Ruft den Wert der syncml-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getSyncml() {
        return syncml;
    }

    /**
     * Legt den Wert der syncml-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setSyncml(JAXBElement<Boolean> value) {
        this.syncml = value;
    }

    /**
     * Ruft den Wert der tasks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getTasks() {
        return tasks;
    }

    /**
     * Legt den Wert der tasks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setTasks(JAXBElement<Boolean> value) {
        this.tasks = value;
    }

    /**
     * Ruft den Wert der vcard-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getVcard() {
        return vcard;
    }

    /**
     * Legt den Wert der vcard-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setVcard(JAXBElement<Boolean> value) {
        this.vcard = value;
    }

    /**
     * Ruft den Wert der webdav-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getWebdav() {
        return webdav;
    }

    /**
     * Legt den Wert der webdav-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setWebdav(JAXBElement<Boolean> value) {
        this.webdav = value;
    }

    /**
     * Ruft den Wert der webdavXml-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getWebdavXml() {
        return webdavXml;
    }

    /**
     * Legt den Wert der webdavXml-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setWebdavXml(JAXBElement<Boolean> value) {
        this.webdavXml = value;
    }

    /**
     * Ruft den Wert der webmail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getWebmail() {
        return webmail;
    }

    /**
     * Legt den Wert der webmail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setWebmail(JAXBElement<Boolean> value) {
        this.webmail = value;
    }

}
