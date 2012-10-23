
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SortDirectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SortDirectionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Ascending"/>
 *     &lt;enumeration value="Descending"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SortDirectionType")
@XmlEnum
public enum SortDirectionType {

    @XmlEnumValue("Ascending")
    ASCENDING("Ascending"),
    @XmlEnumValue("Descending")
    DESCENDING("Descending");
    private final String value;

    SortDirectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SortDirectionType fromValue(String v) {
        for (SortDirectionType c: SortDirectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
