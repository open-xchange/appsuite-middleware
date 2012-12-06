
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarPermissionReadAccessType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CalendarPermissionReadAccessType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="TimeOnly"/>
 *     &lt;enumeration value="TimeAndSubjectAndLocation"/>
 *     &lt;enumeration value="FullDetails"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CalendarPermissionReadAccessType")
@XmlEnum
public enum CalendarPermissionReadAccessType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("TimeOnly")
    TIME_ONLY("TimeOnly"),
    @XmlEnumValue("TimeAndSubjectAndLocation")
    TIME_AND_SUBJECT_AND_LOCATION("TimeAndSubjectAndLocation"),
    @XmlEnumValue("FullDetails")
    FULL_DETAILS("FullDetails");
    private final String value;

    CalendarPermissionReadAccessType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CalendarPermissionReadAccessType fromValue(String v) {
        for (CalendarPermissionReadAccessType c: CalendarPermissionReadAccessType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
