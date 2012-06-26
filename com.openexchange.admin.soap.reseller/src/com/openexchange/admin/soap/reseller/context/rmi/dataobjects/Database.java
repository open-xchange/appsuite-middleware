
package com.openexchange.admin.soap.reseller.context.rmi.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr Database complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Database">
 *   &lt;complexContent>
 *     &lt;extension base="{http://dataobjects.rmi.admin.openexchange.com/xsd}EnforceableDataObject">
 *       &lt;sequence>
 *         &lt;element name="clusterWeight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="clusterWeightset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="currentUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="currentUnitsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="driver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="driverset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="loginset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersChange" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersCreate" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersDelete" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersRegister" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="masterId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="masterIdset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="masterset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="maxUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxUnitsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nameset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passwordset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolHardLimit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolHardLimitset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolInitial" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolInitialset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolMax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolMaxset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="read_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="read_idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="schemeset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="urlset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Database", propOrder = {
    "rest"
})
public class Database
    extends EnforceableDataObject
{

    @XmlElementRefs({
        @XmlElementRef(name = "driverset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "urlset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "scheme", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolHardLimitset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "currentUnitsset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "nameset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "read_idset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolMax", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersChange", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "maxUnits", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "idset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "login", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolInitialset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "name", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "password", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "driver", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersRegister", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "read_id", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersCreate", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolHardLimit", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "loginset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersDelete", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolMaxset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "passwordset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "master", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "url", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "clusterWeight", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterId", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "schemeset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "clusterWeightset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolInitial", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "currentUnits", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "maxUnitsset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterIdset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends Serializable>> rest;

    /**
     * Ruft das restliche Contentmodell ab. 
     * 
     * <p>
     * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
     * Der Feldname "MandatoryMembersChange" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types4
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types4
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung f\u00fcr eine
     * der beiden folgenden Deklarationen an, um deren Namen zu �ndern: 
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
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
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
