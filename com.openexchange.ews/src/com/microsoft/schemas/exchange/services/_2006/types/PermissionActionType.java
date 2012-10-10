
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PermissionActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PermissionActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Owned"/>
 *     &lt;enumeration value="All"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PermissionActionType")
@XmlEnum
public enum PermissionActionType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Owned")
    OWNED("Owned"),
    @XmlEnumValue("All")
    ALL("All");
    private final String value;

    PermissionActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PermissionActionType fromValue(String v) {
        for (PermissionActionType c: PermissionActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
