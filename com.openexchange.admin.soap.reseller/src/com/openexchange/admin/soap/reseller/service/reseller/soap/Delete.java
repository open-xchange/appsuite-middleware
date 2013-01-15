
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
 *         &lt;element name="adm" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}ResellerAdmin" minOccurs="0"/>
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
    "adm",
    "creds"
})
@XmlRootElement(name = "delete")
public class Delete {

    @XmlElement(nillable = true)
    protected ResellerAdmin adm;
    @XmlElement(nillable = true)
    protected Credentials creds;

    /**
     * Ruft den Wert der adm-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link ResellerAdmin }
     *
     */
    public ResellerAdmin getAdm() {
        return adm;
    }

    /**
     * Legt den Wert der adm-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link ResellerAdmin }
     *
     */
    public void setAdm(ResellerAdmin value) {
        this.adm = value;
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
