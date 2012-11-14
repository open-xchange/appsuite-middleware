
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FolderQueryTraversalType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FolderQueryTraversalType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Shallow"/>
 *     &lt;enumeration value="Deep"/>
 *     &lt;enumeration value="SoftDeleted"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FolderQueryTraversalType")
@XmlEnum
public enum FolderQueryTraversalType {

    @XmlEnumValue("Shallow")
    SHALLOW("Shallow"),
    @XmlEnumValue("Deep")
    DEEP("Deep"),
    @XmlEnumValue("SoftDeleted")
    SOFT_DELETED("SoftDeleted");
    private final String value;

    FolderQueryTraversalType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FolderQueryTraversalType fromValue(String v) {
        for (FolderQueryTraversalType c: FolderQueryTraversalType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
