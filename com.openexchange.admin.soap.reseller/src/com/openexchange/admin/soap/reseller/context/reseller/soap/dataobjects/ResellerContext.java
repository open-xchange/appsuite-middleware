
package com.openexchange.admin.soap.reseller.context.reseller.soap.dataobjects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.soap.reseller.context.soap.dataobjects.Context;


/**
 * <p>Java-Klasse f\u00fcr ResellerContext complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="ResellerContext">
 *   &lt;complexContent>
 *     &lt;extension base="{http://dataobjects.soap.admin.openexchange.com/xsd}Context">
 *       &lt;sequence>
 *         &lt;element name="customid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extensionError" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}ResellerAdmin" minOccurs="0"/>
 *         &lt;element name="restriction" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}Restriction" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResellerContext", propOrder = {
    "customid",
    "extensionError",
    "owner",
    "restriction",
    "sid"
})
public class ResellerContext
    extends Context
{

    @XmlElement(nillable = true)
    protected String customid;
    @XmlElement(nillable = true)
    protected String extensionError;
    @XmlElement(nillable = true)
    protected ResellerAdmin owner;
    @XmlElement(nillable = true)
    protected List<Restriction> restriction;
    protected Integer sid;

    /**
     * Ruft den Wert der customid-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCustomid() {
        return customid;
    }

    /**
     * Legt den Wert der customid-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCustomid(String value) {
        this.customid = value;
    }

    /**
     * Ruft den Wert der extensionError-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtensionError() {
        return extensionError;
    }

    /**
     * Legt den Wert der extensionError-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtensionError(String value) {
        this.extensionError = value;
    }

    /**
     * Ruft den Wert der owner-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link ResellerAdmin }
     *
     */
    public ResellerAdmin getOwner() {
        return owner;
    }

    /**
     * Legt den Wert der owner-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link ResellerAdmin }
     *
     */
    public void setOwner(ResellerAdmin value) {
        this.owner = value;
    }

    /**
     * Gets the value of the restriction property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the restriction property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRestriction().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Restriction }
     *
     *
     */
    public List<Restriction> getRestriction() {
        if (restriction == null) {
            restriction = new ArrayList<Restriction>();
        }
        return this.restriction;
    }

    public void setRestriction(List<Restriction> restriction) {
        this.restriction = restriction;
    }

    /**
     * Ruft den Wert der sid-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getSid() {
        return sid;
    }

    /**
     * Legt den Wert der sid-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setSid(Integer value) {
        this.sid = value;
    }

}
