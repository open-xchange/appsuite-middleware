
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Compound property for Managed Folder related information for Managed Folders.
 * 
 * <p>Java class for ManagedFolderInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ManagedFolderInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CanDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CanRenameOrMove" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="MustDisplayComment" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="HasQuota" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsManagedFoldersRoot" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ManagedFolderId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StorageQuota" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="FolderSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="HomePage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManagedFolderInformationType", propOrder = {
    "canDelete",
    "canRenameOrMove",
    "mustDisplayComment",
    "hasQuota",
    "isManagedFoldersRoot",
    "managedFolderId",
    "comment",
    "storageQuota",
    "folderSize",
    "homePage"
})
public class ManagedFolderInformationType {

    @XmlElement(name = "CanDelete")
    protected Boolean canDelete;
    @XmlElement(name = "CanRenameOrMove")
    protected Boolean canRenameOrMove;
    @XmlElement(name = "MustDisplayComment")
    protected Boolean mustDisplayComment;
    @XmlElement(name = "HasQuota")
    protected Boolean hasQuota;
    @XmlElement(name = "IsManagedFoldersRoot")
    protected Boolean isManagedFoldersRoot;
    @XmlElement(name = "ManagedFolderId")
    protected String managedFolderId;
    @XmlElement(name = "Comment")
    protected String comment;
    @XmlElement(name = "StorageQuota")
    protected Integer storageQuota;
    @XmlElement(name = "FolderSize")
    protected Integer folderSize;
    @XmlElement(name = "HomePage")
    protected String homePage;

    /**
     * Gets the value of the canDelete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanDelete() {
        return canDelete;
    }

    /**
     * Sets the value of the canDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanDelete(Boolean value) {
        this.canDelete = value;
    }

    /**
     * Gets the value of the canRenameOrMove property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCanRenameOrMove() {
        return canRenameOrMove;
    }

    /**
     * Sets the value of the canRenameOrMove property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanRenameOrMove(Boolean value) {
        this.canRenameOrMove = value;
    }

    /**
     * Gets the value of the mustDisplayComment property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMustDisplayComment() {
        return mustDisplayComment;
    }

    /**
     * Sets the value of the mustDisplayComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMustDisplayComment(Boolean value) {
        this.mustDisplayComment = value;
    }

    /**
     * Gets the value of the hasQuota property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHasQuota() {
        return hasQuota;
    }

    /**
     * Sets the value of the hasQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasQuota(Boolean value) {
        this.hasQuota = value;
    }

    /**
     * Gets the value of the isManagedFoldersRoot property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsManagedFoldersRoot() {
        return isManagedFoldersRoot;
    }

    /**
     * Sets the value of the isManagedFoldersRoot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsManagedFoldersRoot(Boolean value) {
        this.isManagedFoldersRoot = value;
    }

    /**
     * Gets the value of the managedFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManagedFolderId() {
        return managedFolderId;
    }

    /**
     * Sets the value of the managedFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManagedFolderId(String value) {
        this.managedFolderId = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the storageQuota property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStorageQuota() {
        return storageQuota;
    }

    /**
     * Sets the value of the storageQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStorageQuota(Integer value) {
        this.storageQuota = value;
    }

    /**
     * Gets the value of the folderSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFolderSize() {
        return folderSize;
    }

    /**
     * Sets the value of the folderSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFolderSize(Integer value) {
        this.folderSize = value;
    }

    /**
     * Gets the value of the homePage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomePage() {
        return homePage;
    }

    /**
     * Sets the value of the homePage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomePage(String value) {
        this.homePage = value;
    }

}
