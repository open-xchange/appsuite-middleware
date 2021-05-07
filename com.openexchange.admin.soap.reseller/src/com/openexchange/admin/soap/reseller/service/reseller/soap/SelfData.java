package com.openexchange.admin.soap.reseller.service.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.service.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.soap.reseller.service.rmi.dataobjects.Credentials;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adminName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="adminId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="creds" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
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
    "adminName",
    "adminId",
    "creds"
})
@XmlRootElement(name = "selfData")
public class SelfData {

    @XmlElement(nillable = true)
    protected String adminName;
    @XmlElement(nillable = true)
    protected int adminId;
    @XmlElement(nillable = true)
    protected Credentials creds;

    /**
     * Ruft den Wert der adm-Eigenschaft ab.
     *
     * @return
     *         possible object is
     *         {@link ResellerAdmin }
     *
     */
    public String getAdminName() {
        return adminName;
    }

    /**
     * Legt den Wert der adm-Eigenschaft fest.
     *
     * @param value
     *            allowed object is
     *            {@link ResellerAdmin }
     *
     */
    public void setAdminName(String value) {
        this.adminName = value;
    }

    /**
     * Ruft den Wert der creds-Eigenschaft ab.
     *
     * @return
     *         possible object is
     *         {@link Credentials }
     *
     */
    public Credentials getCreds() {
        return creds;
    }

    /**
     * Legt den Wert der creds-Eigenschaft fest.
     *
     * @param value
     *            allowed object is
     *            {@link Credentials }
     *
     */
    public void setCreds(Credentials value) {
        this.creds = value;
    }

    /**
     * Gets the adminId
     *
     * @return The adminId
     */
    public int getAdminId() {
        return adminId;
    }

    /**
     * Sets the adminId
     *
     * @param adminId The adminId to set
     */
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

}
