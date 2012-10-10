
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharingDataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SharingDataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Calendar"/>
 *     &lt;enumeration value="Contacts"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SharingDataType")
@XmlEnum
public enum SharingDataType {

    @XmlEnumValue("Calendar")
    CALENDAR("Calendar"),
    @XmlEnumValue("Contacts")
    CONTACTS("Contacts");
    private final String value;

    SharingDataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SharingDataType fromValue(String v) {
        for (SharingDataType c: SharingDataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
