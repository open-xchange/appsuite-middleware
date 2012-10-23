
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PhysicalAddressKeyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PhysicalAddressKeyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Business"/>
 *     &lt;enumeration value="Home"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PhysicalAddressKeyType")
@XmlEnum
public enum PhysicalAddressKeyType {

    @XmlEnumValue("Business")
    BUSINESS("Business"),
    @XmlEnumValue("Home")
    HOME("Home"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    PhysicalAddressKeyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PhysicalAddressKeyType fromValue(String v) {
        for (PhysicalAddressKeyType c: PhysicalAddressKeyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
