
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SpecialLogonTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SpecialLogonTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Admin"/>
 *     &lt;enumeration value="SystemService"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SpecialLogonTypeType")
@XmlEnum
public enum SpecialLogonTypeType {

    @XmlEnumValue("Admin")
    ADMIN("Admin"),
    @XmlEnumValue("SystemService")
    SYSTEM_SERVICE("SystemService");
    private final String value;

    SpecialLogonTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SpecialLogonTypeType fromValue(String v) {
        for (SpecialLogonTypeType c: SpecialLogonTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
