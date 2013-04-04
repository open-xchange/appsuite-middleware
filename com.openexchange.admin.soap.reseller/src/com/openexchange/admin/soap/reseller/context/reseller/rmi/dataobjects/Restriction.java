
package com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects;

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
import com.openexchange.admin.soap.reseller.context.rmi.dataobjects.EnforceableDataObject;


/**
 * <p>Java-Klasse f\u00fcr Restriction complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="Restriction">
 *   &lt;complexContent>
 *     &lt;extension base="{http://dataobjects.rmi.admin.openexchange.com/xsd}EnforceableDataObject">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersChange" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersCreate" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersDelete" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersRegister" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Restriction", propOrder = {
    "rest"
})
public class Restriction
    extends EnforceableDataObject
{

    @XmlElementRefs({
        @XmlElementRef(name = "mandatoryMembersRegister", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersChange", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersDelete", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersCreate", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "value", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "name", namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends Serializable>> rest;

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

    public String getValue() {
        return get("value");
    }

    public void setValue(final String value) {
        set("value", "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", String.class, value);
    }

    private <V extends Serializable> void set(final String name, final String namespace, final V value) {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<? extends Serializable>>();
        }
        rest.add(new JAXBElement<V>(new QName(namespace, name), (Class<V>) value.getClass(), value));
    }

    private <V extends Serializable> void set(String name, String namespace, Class<V> clazz, V value) {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<? extends Serializable>>();
        }
        rest.add(new JAXBElement<V>(new QName(namespace, name), clazz, value));
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
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types7
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types4
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung f\u00fcr eine
     * der beiden folgenden Deklarationen an, um deren Namen zu \u00e4ndern:
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
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     *
     */
    public List<JAXBElement<? extends Serializable>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<? extends Serializable>>();
        }
        return this.rest;
    }

}
