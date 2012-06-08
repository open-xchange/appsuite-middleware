
package com.openexchange.admin.soap.reseller.service.reseller.rmi.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import com.openexchange.admin.soap.reseller.service.rmi.dataobjects.EnforceableDataObject;


/**
 * <p>Java-Klasse für ResellerAdmin complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ResellerAdmin">
 *   &lt;complexContent>
 *     &lt;extension base="{http://dataobjects.rmi.admin.openexchange.com/xsd}EnforceableDataObject">
 *       &lt;sequence>
 *         &lt;element name="displayname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displaynameset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersChange" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersCreate" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersDelete" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersRegister" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nameset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="parentId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="parentIdset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passwordMech" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passwordMechset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="passwordset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="restrictions" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}Restriction" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="restrictionsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResellerAdmin", propOrder = {
    "rest"
})
public class ResellerAdmin
    extends EnforceableDataObject
{

    @XmlElementRefs({
        @XmlElementRef(name = "mandatoryMembersDelete", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersRegister", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "passwordMechset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "name", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersCreate", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "passwordMech", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "parentIdset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "displaynameset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "password", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "restrictionsset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "parentId", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersChange", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "restrictions", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "idset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "displayname", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "passwordset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "nameset", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> rest;

    public List<Restriction> getRestrictions() {
        final List<JAXBElement<?>> list = get("restrictions");
        final List<Restriction> ret = new ArrayList<Restriction>(list.size());
        for (final JAXBElement<?> jaxbElement : list) {
            final List<JAXBElement<? extends Serializable>> rest = (List<JAXBElement<? extends Serializable>>) jaxbElement.getValue();
            final Restriction r = new Restriction();
            for (final JAXBElement<? extends Serializable> element : rest) {
                final String localPart = jaxbElement.getName().getLocalPart();
                if ("id".equals(localPart)) {
                    r.setId((Integer) jaxbElement.getValue());
                } else if ("name".equals(localPart)) {
                    r.setName((String) jaxbElement.getValue());
                } else if ("value".equals(localPart)) {
                    r.setValue((String) jaxbElement.getValue());
                }
            }
            ret.add(r);
        }
        return ret;
    }

    public void setRestrictions(final List<Restriction> restrictions) {
        final List<List<JAXBElement<? extends Serializable>>> jaxbRestrictions = new ArrayList<List<JAXBElement<? extends Serializable>>>(restrictions.size());
        for (final Restriction restriction : restrictions) {
            final List<JAXBElement<? extends Serializable>> jaxbRestriction = new ArrayList<JAXBElement<? extends Serializable>>(3);
            jaxbRestriction.add(new JAXBElement<Integer>(new QName("id", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd"), Integer.class, restriction.getId()));
            jaxbRestriction.add(new JAXBElement<String>(new QName("name", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd"), String.class, restriction.getName()));
            jaxbRestriction.add(new JAXBElement<String>(new QName("value", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd"), String.class, restriction.getValue()));
            jaxbRestrictions.add(jaxbRestriction);
        }
        rest.add(new JAXBElement<List>(new QName("restrictions", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd"), List.class, jaxbRestrictions));
    }

    public String getPassword() {
        return get("password");
    }

    public void setPassword(final String value) {
        set("password", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }
    
    public String getPasswordMech() {
        return get("passwordMech");
    }

    public void setPasswordMech(final String value) {
        set("passwordMech", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }
    
    public Integer getParentId() {
        return get("parentId");
    }

    public void setParentId(final Integer value) {
        set("parentId", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }

    public Integer getId() {
        return get("id");
    }

    public void setId(final Integer value) {
        set("id", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }
    
    public String getName() {
        return get("name");
    }

    public void setName(final String value) {
        set("name", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }

    public String getDisplayName() {
        return get("displayname");
    }

    public void setDisplayName(final String value) {
        set("displayname", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", value);
    }

    private <V> void set(final String name, final String namespace, final V value) {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        rest.add(new JAXBElement<V>(new QName(namespace, name), (Class<V>) value.getClass(), value));
    }

    private <V> V get(final String name) {
        if (rest == null) {
            return null;
        }
        for (final JAXBElement<?> element : rest) {
            if (name.equals(element.getName().getLocalPart())) {
                return (V) element.getValue();
            }
        }
        return null;
    }

    /**
     * Ruft das restliche Contentmodell ab. 
     * 
     * <p>
     * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
     * Der Feldname "MandatoryMembersChange" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerService?wsdl#types3
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerService?wsdl#types6
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung für eine
     * der beiden folgenden Deklarationen an, um deren Namen zu ändern: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Restriction }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        return this.rest;
    }

}
