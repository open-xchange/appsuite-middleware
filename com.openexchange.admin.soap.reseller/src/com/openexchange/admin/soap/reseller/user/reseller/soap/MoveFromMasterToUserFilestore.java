
package com.openexchange.admin.soap.reseller.user.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.user.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.soap.reseller.user.rmi.dataobjects.Credentials;
import com.openexchange.admin.soap.reseller.user.soap.dataobjects.Filestore;
import com.openexchange.admin.soap.reseller.user.soap.dataobjects.User;

/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 * &lt;element name="dst_filestore_id" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Filestore" minOccurs="0"/>
 * &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "ctx", "user", "masterUser", "dstFilestoreId", "maxQuota", "auth"
})
@XmlRootElement(name = "moveFromMasterToUserFilestore")
public class MoveFromMasterToUserFilestore {

    @XmlElement(nillable = true)
    protected ResellerContext ctx;
    @XmlElement(nillable = true)
    protected User user;
    @XmlElement(nillable = true)
    protected User masterUser;
    @XmlElement(name = "dst_filestore_id", required = true, nillable = true)
    protected Filestore dstFilestoreId;
    @XmlElement(name = "max_quota", required = true, nillable = true)
    protected Long maxQuota;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     *
     * @return
     *         possible object is
     *         {@link Context }
     *
     */
    public ResellerContext getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     *
     * @param value
     *            allowed object is
     *            {@link Context }
     *
     */
    public void setCtx(ResellerContext value) {
        this.ctx = value;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user
     *
     * @param user The user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the masterUser
     *
     * @return The masterUser
     */
    public User getMasterUser() {
        return masterUser;
    }

    /**
     * Sets the masterUser
     *
     * @param masterUser The masterUser to set
     */
    public void setMasterUser(User masterUser) {
        this.masterUser = masterUser;
    }

    /**
     * Ruft den Wert der auth-Eigenschaft ab.
     *
     * @return
     *         possible object is
     *         {@link Credentials }
     *
     */
    public Credentials getAuth() {
        return auth;
    }

    /**
     * Legt den Wert der auth-Eigenschaft fest.
     *
     * @param value
     *            allowed object is
     *            {@link Credentials }
     *
     */
    public void setAuth(Credentials value) {
        this.auth = value;
    }

    /**
     * Gets the dstFilestoreId
     *
     * @return The dstFilestoreId
     */
    public Filestore getDstFilestoreId() {
        return dstFilestoreId;
    }

    /**
     * Sets the dstFilestoreId
     *
     * @param dstFilestoreId The dstFilestoreId to set
     */
    public void setDstFilestoreId(Filestore dstFilestoreId) {
        this.dstFilestoreId = dstFilestoreId;
    }

    /**
     * Gets the maxQuota
     *
     * @return The maxQuota
     */
    public Long getMaxQuota() {
        return maxQuota;
    }

    /**
     * Sets the maxQuota
     *
     * @param maxQuota The maxQuota to set
     */
    public void setMaxQuota(Long maxQuota) {
        this.maxQuota = maxQuota;
    }

}
