
package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extensionType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="term"/>
 *               &lt;enumeration value="queue"/>
 *               &lt;enumeration value="ivr"/>
 *               &lt;enumeration value="voicecenter"/>
 *               &lt;enumeration value="conference"/>
 *               &lt;enumeration value="queuecenter"/>
 *               &lt;enumeration value="callback"/>
 *               &lt;enumeration value="callcard"/>
 *               &lt;enumeration value="intercom"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="templateID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="serverID" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="filter" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="parentID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="parentIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "extensionType",
    "templateID",
    "serverID",
    "filter",
    "scope",
    "parentID",
    "parentIdentifier"
})
@XmlRootElement(name = "GetExtensionsRequest")
public class GetExtensionsRequest {

    protected String extensionType;
    protected BigInteger templateID;
    protected String serverID;
    protected String filter;
    protected String scope;
    protected BigInteger parentID;
    protected String parentIdentifier;

    /**
     * Gets the value of the extensionType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtensionType() {
        return extensionType;
    }

    /**
     * Sets the value of the extensionType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtensionType(String value) {
        this.extensionType = value;
    }

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
     * Gets the value of the serverID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getServerID() {
        return serverID;
    }

    /**
     * Sets the value of the serverID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setServerID(String value) {
        this.serverID = value;
    }

    /**
     * Gets the value of the filter property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFilter(String value) {
        this.filter = value;
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

}
