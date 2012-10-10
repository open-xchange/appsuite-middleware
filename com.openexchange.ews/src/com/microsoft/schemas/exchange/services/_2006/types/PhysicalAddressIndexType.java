
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PhysicalAddressIndexType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PhysicalAddressIndexType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Business"/>
 *     &lt;enumeration value="Home"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PhysicalAddressIndexType")
@XmlEnum
public enum PhysicalAddressIndexType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Business")
    BUSINESS("Business"),
    @XmlEnumValue("Home")
    HOME("Home"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    PhysicalAddressIndexType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PhysicalAddressIndexType fromValue(String v) {
        for (PhysicalAddressIndexType c: PhysicalAddressIndexType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
