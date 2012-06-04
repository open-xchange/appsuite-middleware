
package com.openexchange.admin.soap.group.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.group.dataobjects.Context;
import com.openexchange.admin.soap.group.dataobjects.Credentials;
import com.openexchange.admin.soap.group.dataobjects.Group;
import com.openexchange.admin.soap.group.dataobjects.User;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 *         &lt;element name="grp" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Group" minOccurs="0"/>
 *         &lt;element name="members" type="{http://dataobjects.soap.admin.openexchange.com/xsd}User" maxOccurs="unbounded" minOccurs="0"/>
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
    "ctx",
    "grp",
    "members",
    "auth"
})
@XmlRootElement(name = "removeMember")
public class RemoveMember {

    @XmlElement(nillable = true)
    protected Context ctx;
    @XmlElement(nillable = true)
    protected Group grp;
    @XmlElement(nillable = true)
    protected List<User> members;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Context }
     *     
     */
    public Context getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Context }
     *     
     */
    public void setCtx(Context value) {
        this.ctx = value;
    }

    /**
     * Ruft den Wert der grp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Group }
     *     
     */
    public Group getGrp() {
        return grp;
    }

    /**
     * Legt den Wert der grp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Group }
     *     
     */
    public void setGrp(Group value) {
        this.grp = value;
    }

    /**
     * Gets the value of the members property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the members property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMembers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link User }
     * 
     * 
     */
    public List<User> getMembers() {
        if (members == null) {
            members = new ArrayList<User>();
        }
        return this.members;
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
