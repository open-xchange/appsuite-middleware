
package com.openexchange.admin.soap.util.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.util.dataobjects.Credentials;
import com.openexchange.admin.soap.util.dataobjects.Database;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "db",
    "auth"
})
@XmlRootElement(name = "unblockDatabase")
public class UnblockDatabase {

    @XmlElement(nillable = true)
    protected Database db;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der db-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Database }
     *
     */
    public Database getDb() {
        return db;
    }

    /**
     * Legt den Wert der db-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Database }
     *
     */
    public void setDb(Database value) {
        this.db = value;
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
