
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ContactItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContactItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}ItemType">
 *       &lt;sequence>
 *         &lt;element name="FileAs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FileAsMapping" type="{http://schemas.microsoft.com/exchange/services/2006/types}FileAsMappingType" minOccurs="0"/>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GivenName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Initials" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MiddleName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Nickname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CompleteName" type="{http://schemas.microsoft.com/exchange/services/2006/types}CompleteNameType" minOccurs="0"/>
 *         &lt;element name="CompanyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EmailAddresses" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressDictionaryType" minOccurs="0"/>
 *         &lt;element name="PhysicalAddresses" type="{http://schemas.microsoft.com/exchange/services/2006/types}PhysicalAddressDictionaryType" minOccurs="0"/>
 *         &lt;element name="PhoneNumbers" type="{http://schemas.microsoft.com/exchange/services/2006/types}PhoneNumberDictionaryType" minOccurs="0"/>
 *         &lt;element name="AssistantName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Birthday" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="BusinessHomePage" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Children" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="Companies" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfStringsType" minOccurs="0"/>
 *         &lt;element name="ContactSource" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContactSourceType" minOccurs="0"/>
 *         &lt;element name="Department" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Generation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ImAddresses" type="{http://schemas.microsoft.com/exchange/services/2006/types}ImAddressDictionaryType" minOccurs="0"/>
 *         &lt;element name="JobTitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Manager" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Mileage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OfficeLocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PostalAddressIndex" type="{http://schemas.microsoft.com/exchange/services/2006/types}PhysicalAddressIndexType" minOccurs="0"/>
 *         &lt;element name="Profession" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SpouseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Surname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WeddingAnniversary" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="HasPicture" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="PhoneticFullName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PhoneticFirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PhoneticLastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Alias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Photo" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="UserSMIMECertificate" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfBinaryType" minOccurs="0"/>
 *         &lt;element name="MSExchangeCertificate" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfBinaryType" minOccurs="0"/>
 *         &lt;element name="DirectoryId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ManagerMailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="DirectReports" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactItemType", propOrder = {
    "fileAs",
    "fileAsMapping",
    "displayName",
    "givenName",
    "initials",
    "middleName",
    "nickname",
    "completeName",
    "companyName",
    "emailAddresses",
    "physicalAddresses",
    "phoneNumbers",
    "assistantName",
    "birthday",
    "businessHomePage",
    "children",
    "companies",
    "contactSource",
    "department",
    "generation",
    "imAddresses",
    "jobTitle",
    "manager",
    "mileage",
    "officeLocation",
    "postalAddressIndex",
    "profession",
    "spouseName",
    "surname",
    "weddingAnniversary",
    "hasPicture",
    "phoneticFullName",
    "phoneticFirstName",
    "phoneticLastName",
    "alias",
    "notes",
    "photo",
    "userSMIMECertificate",
    "msExchangeCertificate",
    "directoryId",
    "managerMailbox",
    "directReports"
})
public class ContactItemType
    extends ItemType
{

    @XmlElement(name = "FileAs")
    protected String fileAs;
    @XmlElement(name = "FileAsMapping")
    protected FileAsMappingType fileAsMapping;
    @XmlElement(name = "DisplayName")
    protected String displayName;
    @XmlElement(name = "GivenName")
    protected String givenName;
    @XmlElement(name = "Initials")
    protected String initials;
    @XmlElement(name = "MiddleName")
    protected String middleName;
    @XmlElement(name = "Nickname")
    protected String nickname;
    @XmlElement(name = "CompleteName")
    protected CompleteNameType completeName;
    @XmlElement(name = "CompanyName")
    protected String companyName;
    @XmlElement(name = "EmailAddresses")
    protected EmailAddressDictionaryType emailAddresses;
    @XmlElement(name = "PhysicalAddresses")
    protected PhysicalAddressDictionaryType physicalAddresses;
    @XmlElement(name = "PhoneNumbers")
    protected PhoneNumberDictionaryType phoneNumbers;
    @XmlElement(name = "AssistantName")
    protected String assistantName;
    @XmlElement(name = "Birthday")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar birthday;
    @XmlElement(name = "BusinessHomePage")
    @XmlSchemaType(name = "anyURI")
    protected String businessHomePage;
    @XmlElement(name = "Children")
    protected ArrayOfStringsType children;
    @XmlElement(name = "Companies")
    protected ArrayOfStringsType companies;
    @XmlElement(name = "ContactSource")
    protected ContactSourceType contactSource;
    @XmlElement(name = "Department")
    protected String department;
    @XmlElement(name = "Generation")
    protected String generation;
    @XmlElement(name = "ImAddresses")
    protected ImAddressDictionaryType imAddresses;
    @XmlElement(name = "JobTitle")
    protected String jobTitle;
    @XmlElement(name = "Manager")
    protected String manager;
    @XmlElement(name = "Mileage")
    protected String mileage;
    @XmlElement(name = "OfficeLocation")
    protected String officeLocation;
    @XmlElement(name = "PostalAddressIndex")
    protected PhysicalAddressIndexType postalAddressIndex;
    @XmlElement(name = "Profession")
    protected String profession;
    @XmlElement(name = "SpouseName")
    protected String spouseName;
    @XmlElement(name = "Surname")
    protected String surname;
    @XmlElement(name = "WeddingAnniversary")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar weddingAnniversary;
    @XmlElement(name = "HasPicture")
    protected Boolean hasPicture;
    @XmlElement(name = "PhoneticFullName")
    protected String phoneticFullName;
    @XmlElement(name = "PhoneticFirstName")
    protected String phoneticFirstName;
    @XmlElement(name = "PhoneticLastName")
    protected String phoneticLastName;
    @XmlElement(name = "Alias")
    protected String alias;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlElement(name = "Photo")
    protected byte[] photo;
    @XmlElement(name = "UserSMIMECertificate")
    protected ArrayOfBinaryType userSMIMECertificate;
    @XmlElement(name = "MSExchangeCertificate")
    protected ArrayOfBinaryType msExchangeCertificate;
    @XmlElement(name = "DirectoryId")
    protected String directoryId;
    @XmlElement(name = "ManagerMailbox")
    protected SingleRecipientType managerMailbox;
    @XmlElement(name = "DirectReports")
    protected ArrayOfRecipientsType directReports;

    /**
     * Gets the value of the fileAs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileAs() {
        return fileAs;
    }

    /**
     * Sets the value of the fileAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileAs(String value) {
        this.fileAs = value;
    }

    /**
     * Gets the value of the fileAsMapping property.
     * 
     * @return
     *     possible object is
     *     {@link FileAsMappingType }
     *     
     */
    public FileAsMappingType getFileAsMapping() {
        return fileAsMapping;
    }

    /**
     * Sets the value of the fileAsMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileAsMappingType }
     *     
     */
    public void setFileAsMapping(FileAsMappingType value) {
        this.fileAsMapping = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the givenName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets the value of the givenName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGivenName(String value) {
        this.givenName = value;
    }

    /**
     * Gets the value of the initials property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Sets the value of the initials property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitials(String value) {
        this.initials = value;
    }

    /**
     * Gets the value of the middleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the value of the middleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiddleName(String value) {
        this.middleName = value;
    }

    /**
     * Gets the value of the nickname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the value of the nickname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNickname(String value) {
        this.nickname = value;
    }

    /**
     * Gets the value of the completeName property.
     * 
     * @return
     *     possible object is
     *     {@link CompleteNameType }
     *     
     */
    public CompleteNameType getCompleteName() {
        return completeName;
    }

    /**
     * Sets the value of the completeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompleteNameType }
     *     
     */
    public void setCompleteName(CompleteNameType value) {
        this.completeName = value;
    }

    /**
     * Gets the value of the companyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the value of the companyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyName(String value) {
        this.companyName = value;
    }

    /**
     * Gets the value of the emailAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressDictionaryType }
     *     
     */
    public EmailAddressDictionaryType getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * Sets the value of the emailAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressDictionaryType }
     *     
     */
    public void setEmailAddresses(EmailAddressDictionaryType value) {
        this.emailAddresses = value;
    }

    /**
     * Gets the value of the physicalAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link PhysicalAddressDictionaryType }
     *     
     */
    public PhysicalAddressDictionaryType getPhysicalAddresses() {
        return physicalAddresses;
    }

    /**
     * Sets the value of the physicalAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhysicalAddressDictionaryType }
     *     
     */
    public void setPhysicalAddresses(PhysicalAddressDictionaryType value) {
        this.physicalAddresses = value;
    }

    /**
     * Gets the value of the phoneNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneNumberDictionaryType }
     *     
     */
    public PhoneNumberDictionaryType getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Sets the value of the phoneNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneNumberDictionaryType }
     *     
     */
    public void setPhoneNumbers(PhoneNumberDictionaryType value) {
        this.phoneNumbers = value;
    }

    /**
     * Gets the value of the assistantName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssistantName() {
        return assistantName;
    }

    /**
     * Sets the value of the assistantName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssistantName(String value) {
        this.assistantName = value;
    }

    /**
     * Gets the value of the birthday property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBirthday() {
        return birthday;
    }

    /**
     * Sets the value of the birthday property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBirthday(XMLGregorianCalendar value) {
        this.birthday = value;
    }

    /**
     * Gets the value of the businessHomePage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusinessHomePage() {
        return businessHomePage;
    }

    /**
     * Sets the value of the businessHomePage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusinessHomePage(String value) {
        this.businessHomePage = value;
    }

    /**
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public ArrayOfStringsType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfStringsType }
     *     
     */
    public void setChildren(ArrayOfStringsType value) {
        this.children = value;
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
     * Gets the value of the contactSource property.
     * 
     * @return
     *     possible object is
     *     {@link ContactSourceType }
     *     
     */
    public ContactSourceType getContactSource() {
        return contactSource;
    }

    /**
     * Sets the value of the contactSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactSourceType }
     *     
     */
    public void setContactSource(ContactSourceType value) {
        this.contactSource = value;
    }

    /**
     * Gets the value of the department property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the value of the department property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartment(String value) {
        this.department = value;
    }

    /**
     * Gets the value of the generation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneration() {
        return generation;
    }

    /**
     * Sets the value of the generation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneration(String value) {
        this.generation = value;
    }

    /**
     * Gets the value of the imAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link ImAddressDictionaryType }
     *     
     */
    public ImAddressDictionaryType getImAddresses() {
        return imAddresses;
    }

    /**
     * Sets the value of the imAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ImAddressDictionaryType }
     *     
     */
    public void setImAddresses(ImAddressDictionaryType value) {
        this.imAddresses = value;
    }

    /**
     * Gets the value of the jobTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the value of the jobTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobTitle(String value) {
        this.jobTitle = value;
    }

    /**
     * Gets the value of the manager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManager() {
        return manager;
    }

    /**
     * Sets the value of the manager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManager(String value) {
        this.manager = value;
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
     * Gets the value of the officeLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficeLocation() {
        return officeLocation;
    }

    /**
     * Sets the value of the officeLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficeLocation(String value) {
        this.officeLocation = value;
    }

    /**
     * Gets the value of the postalAddressIndex property.
     * 
     * @return
     *     possible object is
     *     {@link PhysicalAddressIndexType }
     *     
     */
    public PhysicalAddressIndexType getPostalAddressIndex() {
        return postalAddressIndex;
    }

    /**
     * Sets the value of the postalAddressIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhysicalAddressIndexType }
     *     
     */
    public void setPostalAddressIndex(PhysicalAddressIndexType value) {
        this.postalAddressIndex = value;
    }

    /**
     * Gets the value of the profession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProfession() {
        return profession;
    }

    /**
     * Sets the value of the profession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProfession(String value) {
        this.profession = value;
    }

    /**
     * Gets the value of the spouseName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpouseName() {
        return spouseName;
    }

    /**
     * Sets the value of the spouseName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpouseName(String value) {
        this.spouseName = value;
    }

    /**
     * Gets the value of the surname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the value of the surname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSurname(String value) {
        this.surname = value;
    }

    /**
     * Gets the value of the weddingAnniversary property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getWeddingAnniversary() {
        return weddingAnniversary;
    }

    /**
     * Sets the value of the weddingAnniversary property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setWeddingAnniversary(XMLGregorianCalendar value) {
        this.weddingAnniversary = value;
    }

    /**
     * Gets the value of the hasPicture property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHasPicture() {
        return hasPicture;
    }

    /**
     * Sets the value of the hasPicture property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasPicture(Boolean value) {
        this.hasPicture = value;
    }

    /**
     * Gets the value of the phoneticFullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneticFullName() {
        return phoneticFullName;
    }

    /**
     * Sets the value of the phoneticFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneticFullName(String value) {
        this.phoneticFullName = value;
    }

    /**
     * Gets the value of the phoneticFirstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneticFirstName() {
        return phoneticFirstName;
    }

    /**
     * Sets the value of the phoneticFirstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneticFirstName(String value) {
        this.phoneticFirstName = value;
    }

    /**
     * Gets the value of the phoneticLastName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneticLastName() {
        return phoneticLastName;
    }

    /**
     * Sets the value of the phoneticLastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneticLastName(String value) {
        this.phoneticLastName = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the photo property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getPhoto() {
        return photo;
    }

    /**
     * Sets the value of the photo property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setPhoto(byte[] value) {
        this.photo = ((byte[]) value);
    }

    /**
     * Gets the value of the userSMIMECertificate property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfBinaryType }
     *     
     */
    public ArrayOfBinaryType getUserSMIMECertificate() {
        return userSMIMECertificate;
    }

    /**
     * Sets the value of the userSMIMECertificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfBinaryType }
     *     
     */
    public void setUserSMIMECertificate(ArrayOfBinaryType value) {
        this.userSMIMECertificate = value;
    }

    /**
     * Gets the value of the msExchangeCertificate property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfBinaryType }
     *     
     */
    public ArrayOfBinaryType getMSExchangeCertificate() {
        return msExchangeCertificate;
    }

    /**
     * Sets the value of the msExchangeCertificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfBinaryType }
     *     
     */
    public void setMSExchangeCertificate(ArrayOfBinaryType value) {
        this.msExchangeCertificate = value;
    }

    /**
     * Gets the value of the directoryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectoryId() {
        return directoryId;
    }

    /**
     * Sets the value of the directoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectoryId(String value) {
        this.directoryId = value;
    }

    /**
     * Gets the value of the managerMailbox property.
     * 
     * @return
     *     possible object is
     *     {@link SingleRecipientType }
     *     
     */
    public SingleRecipientType getManagerMailbox() {
        return managerMailbox;
    }

    /**
     * Sets the value of the managerMailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleRecipientType }
     *     
     */
    public void setManagerMailbox(SingleRecipientType value) {
        this.managerMailbox = value;
    }

    /**
     * Gets the value of the directReports property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public ArrayOfRecipientsType getDirectReports() {
        return directReports;
    }

    /**
     * Sets the value of the directReports property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRecipientsType }
     *     
     */
    public void setDirectReports(ArrayOfRecipientsType value) {
        this.directReports = value;
    }

}
