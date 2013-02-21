
package com.openexchange.admin.soap.reseller.service.reseller.soap;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="admins" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}ResellerAdmin" maxOccurs="unbounded" minOccurs="0"/>
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
    "admins",
    "creds"
})
@XmlRootElement(name = "getMultipleData")
public class GetMultipleData {

    @XmlElement(nillable = true)
    protected List<ResellerAdmin> admins;
    @XmlElement(nillable = true)
    protected Credentials creds;

    /**
     * Gets the value of the admins property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the admins property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdmins().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResellerAdmin }
     *
     *
     */
    public List<ResellerAdmin> getAdmins() {
        if (admins == null) {
            admins = new ArrayList<ResellerAdmin>();
        }
        return this.admins;
    }

    /**
     * Ruft den Wert der creds-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Credentials }
     *
     */
    public Credentials getCreds() {
        return creds;
    }

    /**
     * Legt den Wert der creds-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Credentials }
     *
     */
    public void setCreds(Credentials value) {
        this.creds = value;
    }

}
