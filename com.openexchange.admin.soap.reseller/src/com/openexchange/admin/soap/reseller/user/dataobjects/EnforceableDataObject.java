
package com.openexchange.admin.soap.reseller.user.dataobjects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EnforceableDataObject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EnforceableDataObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mandatoryMembersChange" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersCreate" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersDelete" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersRegister" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnforceableDataObject", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", propOrder = {
    "mandatoryMembersChange",
    "mandatoryMembersCreate",
    "mandatoryMembersDelete",
    "mandatoryMembersRegister"
})
@XmlSeeAlso({
    ResellerAdmin.class,
    Restriction.class
})
public class EnforceableDataObject {

    @XmlElement(nillable = true)
    protected List<String> mandatoryMembersChange;
    @XmlElement(nillable = true)
    protected List<String> mandatoryMembersCreate;
    @XmlElement(nillable = true)
    protected List<String> mandatoryMembersDelete;
    @XmlElement(nillable = true)
    protected List<String> mandatoryMembersRegister;

    /**
     * Gets the value of the mandatoryMembersChange property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandatoryMembersChange property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandatoryMembersChange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMandatoryMembersChange() {
        if (mandatoryMembersChange == null) {
            mandatoryMembersChange = new ArrayList<String>();
        }
        return this.mandatoryMembersChange;
    }

    /**
     * Gets the value of the mandatoryMembersCreate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandatoryMembersCreate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandatoryMembersCreate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMandatoryMembersCreate() {
        if (mandatoryMembersCreate == null) {
            mandatoryMembersCreate = new ArrayList<String>();
        }
        return this.mandatoryMembersCreate;
    }

    /**
     * Gets the value of the mandatoryMembersDelete property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandatoryMembersDelete property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandatoryMembersDelete().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMandatoryMembersDelete() {
        if (mandatoryMembersDelete == null) {
            mandatoryMembersDelete = new ArrayList<String>();
        }
        return this.mandatoryMembersDelete;
    }

    /**
     * Gets the value of the mandatoryMembersRegister property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandatoryMembersRegister property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandatoryMembersRegister().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMandatoryMembersRegister() {
        if (mandatoryMembersRegister == null) {
            mandatoryMembersRegister = new ArrayList<String>();
        }
        return this.mandatoryMembersRegister;
    }

}
