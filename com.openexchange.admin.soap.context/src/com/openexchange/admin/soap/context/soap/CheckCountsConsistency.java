
package com.openexchange.admin.soap.context.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.context.dataobjects.Credentials;


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
 *         &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
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
    "checkDatabaseCounts",
    "checkFilestoreCounts",
    "auth"
})
@XmlRootElement(name = "checkCountsConsistency")
public class CheckCountsConsistency {

    @XmlElement(nillable = true)
    protected Credentials auth;
    @XmlElement(nillable = true)
    protected boolean checkDatabaseCounts;
    @XmlElement(nillable = true)
    protected boolean checkFilestoreCounts;

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


    /**
     * Gets the checkDatabaseCounts
     *
     * @return The checkDatabaseCounts
     */
    public boolean isCheckDatabaseCounts() {
        return checkDatabaseCounts;
    }


    /**
     * Sets the checkDatabaseCounts
     *
     * @param checkDatabaseCounts The checkDatabaseCounts to set
     */
    public void setCheckDatabaseCounts(boolean checkDatabaseCounts) {
        this.checkDatabaseCounts = checkDatabaseCounts;
    }


    /**
     * Gets the checkFilestoreCounts
     *
     * @return The checkFilestoreCounts
     */
    public boolean isCheckFilestoreCounts() {
        return checkFilestoreCounts;
    }


    /**
     * Sets the checkFilestoreCounts
     *
     * @param checkFilestoreCounts The checkFilestoreCounts to set
     */
    public void setCheckFilestoreCounts(boolean checkFilestoreCounts) {
        this.checkFilestoreCounts = checkFilestoreCounts;
    }

}
