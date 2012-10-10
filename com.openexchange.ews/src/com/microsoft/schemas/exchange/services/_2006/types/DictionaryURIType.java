
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DictionaryURIType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DictionaryURIType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="item:InternetMessageHeader"/>
 *     &lt;enumeration value="contacts:ImAddress"/>
 *     &lt;enumeration value="contacts:PhysicalAddress:Street"/>
 *     &lt;enumeration value="contacts:PhysicalAddress:City"/>
 *     &lt;enumeration value="contacts:PhysicalAddress:State"/>
 *     &lt;enumeration value="contacts:PhysicalAddress:CountryOrRegion"/>
 *     &lt;enumeration value="contacts:PhysicalAddress:PostalCode"/>
 *     &lt;enumeration value="contacts:PhoneNumber"/>
 *     &lt;enumeration value="contacts:EmailAddress"/>
 *     &lt;enumeration value="distributionlist:Members:Member"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DictionaryURIType")
@XmlEnum
public enum DictionaryURIType {

    @XmlEnumValue("item:InternetMessageHeader")
    ITEM_INTERNET_MESSAGE_HEADER("item:InternetMessageHeader"),
    @XmlEnumValue("contacts:ImAddress")
    CONTACTS_IM_ADDRESS("contacts:ImAddress"),
    @XmlEnumValue("contacts:PhysicalAddress:Street")
    CONTACTS_PHYSICAL_ADDRESS_STREET("contacts:PhysicalAddress:Street"),
    @XmlEnumValue("contacts:PhysicalAddress:City")
    CONTACTS_PHYSICAL_ADDRESS_CITY("contacts:PhysicalAddress:City"),
    @XmlEnumValue("contacts:PhysicalAddress:State")
    CONTACTS_PHYSICAL_ADDRESS_STATE("contacts:PhysicalAddress:State"),
    @XmlEnumValue("contacts:PhysicalAddress:CountryOrRegion")
    CONTACTS_PHYSICAL_ADDRESS_COUNTRY_OR_REGION("contacts:PhysicalAddress:CountryOrRegion"),
    @XmlEnumValue("contacts:PhysicalAddress:PostalCode")
    CONTACTS_PHYSICAL_ADDRESS_POSTAL_CODE("contacts:PhysicalAddress:PostalCode"),
    @XmlEnumValue("contacts:PhoneNumber")
    CONTACTS_PHONE_NUMBER("contacts:PhoneNumber"),
    @XmlEnumValue("contacts:EmailAddress")
    CONTACTS_EMAIL_ADDRESS("contacts:EmailAddress"),
    @XmlEnumValue("distributionlist:Members:Member")
    DISTRIBUTIONLIST_MEMBERS_MEMBER("distributionlist:Members:Member");
    private final String value;

    DictionaryURIType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DictionaryURIType fromValue(String v) {
        for (DictionaryURIType c: DictionaryURIType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
