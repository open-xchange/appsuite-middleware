
package com._4psa.headerdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="edition" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="version" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="infrastructureID" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
    "edition",
    "version",
    "infrastructureID"
})
@XmlRootElement(name = "serverInfo")
public class ServerInfo {

    @XmlElement(required = true)
    protected String edition;
    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected String infrastructureID;

    /**
     * Gets the value of the edition property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEdition() {
        return edition;
    }

    /**
     * Sets the value of the edition property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEdition(String value) {
        this.edition = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the infrastructureID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInfrastructureID() {
        return infrastructureID;
    }

    /**
     * Sets the value of the infrastructureID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInfrastructureID(String value) {
        this.infrastructureID = value;
    }

}
