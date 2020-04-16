
package com.openexchange.admin.soap.reseller.context.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.context.rmi.dataobjects.Credentials;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "offset",
    "length",
    "auth"
})
@XmlRootElement(name = "listPageAll")
public class ListPageAll {

    @XmlElement(name = "offset", nillable = true)
    protected String offset;
    @XmlElement(name = "length", nillable = true)
    protected String length;
    @XmlElement(nillable = true)
    protected Credentials auth;

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    /**
     * Ruft den Wert der auth-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Credentials }
     *
     */
    public Credentials getAuth() {
        return auth;
    }

    /**
     * Legt den Wert der auth-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Credentials }
     *
     */
    public void setAuth(Credentials value) {
        this.auth = value;
    }

}
