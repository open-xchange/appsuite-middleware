
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="MajorVersion" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="MinorVersion" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="MajorBuildNumber" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="MinorBuildNumber" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="Version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ServerVersionInfo")
public class ServerVersionInfo {

    @XmlAttribute(name = "MajorVersion")
    protected Integer majorVersion;
    @XmlAttribute(name = "MinorVersion")
    protected Integer minorVersion;
    @XmlAttribute(name = "MajorBuildNumber")
    protected Integer majorBuildNumber;
    @XmlAttribute(name = "MinorBuildNumber")
    protected Integer minorBuildNumber;
    @XmlAttribute(name = "Version")
    protected String version;

    /**
     * Gets the value of the majorVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMajorVersion() {
        return majorVersion;
    }

    /**
     * Sets the value of the majorVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMajorVersion(Integer value) {
        this.majorVersion = value;
    }

    /**
     * Gets the value of the minorVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinorVersion() {
        return minorVersion;
    }

    /**
     * Sets the value of the minorVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinorVersion(Integer value) {
        this.minorVersion = value;
    }

    /**
     * Gets the value of the majorBuildNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMajorBuildNumber() {
        return majorBuildNumber;
    }

    /**
     * Sets the value of the majorBuildNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMajorBuildNumber(Integer value) {
        this.majorBuildNumber = value;
    }

    /**
     * Gets the value of the minorBuildNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinorBuildNumber() {
        return minorBuildNumber;
    }

    /**
     * Sets the value of the minorBuildNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinorBuildNumber(Integer value) {
        this.minorBuildNumber = value;
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

}
