
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DelegateFolderPermissionLevelType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DelegateFolderPermissionLevelType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Editor"/>
 *     &lt;enumeration value="Reviewer"/>
 *     &lt;enumeration value="Author"/>
 *     &lt;enumeration value="Custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DelegateFolderPermissionLevelType")
@XmlEnum
public enum DelegateFolderPermissionLevelType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Editor")
    EDITOR("Editor"),
    @XmlEnumValue("Reviewer")
    REVIEWER("Reviewer"),
    @XmlEnumValue("Author")
    AUTHOR("Author"),
    @XmlEnumValue("Custom")
    CUSTOM("Custom");
    private final String value;

    DelegateFolderPermissionLevelType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DelegateFolderPermissionLevelType fromValue(String v) {
        for (DelegateFolderPermissionLevelType c: DelegateFolderPermissionLevelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
