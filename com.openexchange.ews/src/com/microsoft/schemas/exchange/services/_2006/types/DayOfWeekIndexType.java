
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DayOfWeekIndexType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DayOfWeekIndexType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="First"/>
 *     &lt;enumeration value="Second"/>
 *     &lt;enumeration value="Third"/>
 *     &lt;enumeration value="Fourth"/>
 *     &lt;enumeration value="Last"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DayOfWeekIndexType")
@XmlEnum
public enum DayOfWeekIndexType {

    @XmlEnumValue("First")
    FIRST("First"),
    @XmlEnumValue("Second")
    SECOND("Second"),
    @XmlEnumValue("Third")
    THIRD("Third"),
    @XmlEnumValue("Fourth")
    FOURTH("Fourth"),
    @XmlEnumValue("Last")
    LAST("Last");
    private final String value;

    DayOfWeekIndexType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DayOfWeekIndexType fromValue(String v) {
        for (DayOfWeekIndexType c: DayOfWeekIndexType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
