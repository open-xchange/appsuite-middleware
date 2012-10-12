
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DistinguishedPropertySetType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DistinguishedPropertySetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Meeting"/>
 *     &lt;enumeration value="Appointment"/>
 *     &lt;enumeration value="Common"/>
 *     &lt;enumeration value="PublicStrings"/>
 *     &lt;enumeration value="Address"/>
 *     &lt;enumeration value="InternetHeaders"/>
 *     &lt;enumeration value="CalendarAssistant"/>
 *     &lt;enumeration value="UnifiedMessaging"/>
 *     &lt;enumeration value="Task"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DistinguishedPropertySetType")
@XmlEnum
public enum DistinguishedPropertySetType {

    @XmlEnumValue("Meeting")
    MEETING("Meeting"),
    @XmlEnumValue("Appointment")
    APPOINTMENT("Appointment"),
    @XmlEnumValue("Common")
    COMMON("Common"),
    @XmlEnumValue("PublicStrings")
    PUBLIC_STRINGS("PublicStrings"),
    @XmlEnumValue("Address")
    ADDRESS("Address"),
    @XmlEnumValue("InternetHeaders")
    INTERNET_HEADERS("InternetHeaders"),
    @XmlEnumValue("CalendarAssistant")
    CALENDAR_ASSISTANT("CalendarAssistant"),
    @XmlEnumValue("UnifiedMessaging")
    UNIFIED_MESSAGING("UnifiedMessaging"),
    @XmlEnumValue("Task")
    TASK("Task");
    private final String value;

    DistinguishedPropertySetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DistinguishedPropertySetType fromValue(String v) {
        for (DistinguishedPropertySetType c: DistinguishedPropertySetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
