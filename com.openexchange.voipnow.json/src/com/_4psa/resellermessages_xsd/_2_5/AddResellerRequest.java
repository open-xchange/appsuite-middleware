
package com._4psa.resellermessages_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.resellerdata_xsd._2_5.ResellerInfo;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ResellerData.xsd/2.5.1}ResellerInfo">
 *       &lt;sequence>
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="verbose" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="notifyOnly" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="linkResourceID" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="linkUUID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="dku" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="accountFlag" maxOccurs="unbounded" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="noneditablelogin"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "templateID",
    "parentID",
    "parentIdentifier",
    "verbose",
    "notifyOnly",
    "scope",
    "linkResourceID",
    "linkUUID",
    "dku",
    "accountFlag"
})
@XmlRootElement(name = "AddResellerRequest")
public class AddResellerRequest
    extends ResellerInfo
{

    protected BigInteger templateID;
    protected BigInteger parentID;
    protected String parentIdentifier;
    @XmlElement(defaultValue = "0")
    protected Boolean verbose;
    protected BigDecimal notifyOnly;
    protected String scope;
    @XmlElement(defaultValue = "1")
    protected BigInteger linkResourceID;
    protected String linkUUID;
    protected String dku;
    @XmlElement(defaultValue = "noneditablelogin")
    protected List<String> accountFlag;

    /**
     * Gets the value of the templateID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTemplateID() {
        return templateID;
    }

    /**
     * Sets the value of the templateID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTemplateID(BigInteger value) {
        this.templateID = value;
    }

    /**
     * Gets the value of the parentID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getParentID() {
        return parentID;
    }

    /**
     * Sets the value of the parentID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setParentID(BigInteger value) {
        this.parentID = value;
    }

    /**
     * Gets the value of the parentIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Sets the value of the parentIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentIdentifier(String value) {
        this.parentIdentifier = value;
    }

    /**
     * Gets the value of the verbose property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the value of the verbose property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVerbose(Boolean value) {
        this.verbose = value;
    }

    /**
     * Gets the value of the notifyOnly property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getNotifyOnly() {
        return notifyOnly;
    }

    /**
     * Sets the value of the notifyOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setNotifyOnly(BigDecimal value) {
        this.notifyOnly = value;
    }

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the linkResourceID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLinkResourceID() {
        return linkResourceID;
    }

    /**
     * Sets the value of the linkResourceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLinkResourceID(BigInteger value) {
        this.linkResourceID = value;
    }

    /**
     * Gets the value of the linkUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkUUID() {
        return linkUUID;
    }

    /**
     * Sets the value of the linkUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkUUID(String value) {
        this.linkUUID = value;
    }

    /**
     * Gets the value of the dku property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDku() {
        return dku;
    }

    /**
     * Sets the value of the dku property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDku(String value) {
        this.dku = value;
    }

    /**
     * Gets the value of the accountFlag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accountFlag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccountFlag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAccountFlag() {
        if (accountFlag == null) {
            accountFlag = new ArrayList<String>();
        }
        return this.accountFlag;
    }

}
