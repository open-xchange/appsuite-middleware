
package com._4psa.pbxmessages_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
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
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="URL" type="{http://4psa.com/Common.xsd/2.5.1}domain"/>
 *         &lt;element name="masterAuth" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="masterAuthType" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="param" maxOccurs="64" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded">
 *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "scope",
    "url",
    "masterAuth",
    "masterAuthType",
    "param"
})
@XmlRootElement(name = "EnrollRequest")
public class EnrollRequest {

    @XmlElement(required = true)
    protected String scope;
    @XmlElement(name = "URL", required = true)
    protected String url;
    @XmlElement(required = true)
    protected String masterAuth;
    @XmlElement(required = true)
    protected String masterAuthType;
    protected List<EnrollRequest.Param> param;

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
     * Gets the value of the url property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setURL(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the masterAuth property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMasterAuth() {
        return masterAuth;
    }

    /**
     * Sets the value of the masterAuth property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMasterAuth(String value) {
        this.masterAuth = value;
    }

    /**
     * Gets the value of the masterAuthType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMasterAuthType() {
        return masterAuthType;
    }

    /**
     * Sets the value of the masterAuthType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMasterAuthType(String value) {
        this.masterAuthType = value;
    }

    /**
     * Gets the value of the param property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnrollRequest.Param }
     *
     *
     */
    public List<EnrollRequest.Param> getParam() {
        if (param == null) {
            param = new ArrayList<EnrollRequest.Param>();
        }
        return this.param;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded">
     *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "nameAndValue"
    })
    public static class Param {

        @XmlElementRefs({
            @XmlElementRef(name = "name", namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", type = JAXBElement.class),
            @XmlElementRef(name = "value", namespace = "http://4psa.com/PBXMessages.xsd/2.5.1", type = JAXBElement.class)
        })
        protected List<JAXBElement<String>> nameAndValue;

        /**
         * Gets the value of the nameAndValue property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the nameAndValue property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNameAndValue().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link String }{@code >}
         * {@link JAXBElement }{@code <}{@link String }{@code >}
         *
         *
         */
        public List<JAXBElement<String>> getNameAndValue() {
            if (nameAndValue == null) {
                nameAndValue = new ArrayList<JAXBElement<String>>();
            }
            return this.nameAndValue;
        }

    }

}
