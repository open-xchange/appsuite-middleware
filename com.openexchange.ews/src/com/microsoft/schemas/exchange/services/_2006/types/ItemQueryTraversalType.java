
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ItemQueryTraversalType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ItemQueryTraversalType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Shallow"/>
 *     &lt;enumeration value="SoftDeleted"/>
 *     &lt;enumeration value="Associated"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ItemQueryTraversalType")
@XmlEnum
public enum ItemQueryTraversalType {

    @XmlEnumValue("Shallow")
    SHALLOW("Shallow"),
    @XmlEnumValue("SoftDeleted")
    SOFT_DELETED("SoftDeleted"),
    @XmlEnumValue("Associated")
    ASSOCIATED("Associated");
    private final String value;

    ItemQueryTraversalType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ItemQueryTraversalType fromValue(String v) {
        for (ItemQueryTraversalType c: ItemQueryTraversalType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
