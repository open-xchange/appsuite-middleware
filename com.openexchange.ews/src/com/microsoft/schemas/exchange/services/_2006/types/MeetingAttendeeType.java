
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MeetingAttendeeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MeetingAttendeeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Organizer"/>
 *     &lt;enumeration value="Required"/>
 *     &lt;enumeration value="Optional"/>
 *     &lt;enumeration value="Room"/>
 *     &lt;enumeration value="Resource"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MeetingAttendeeType")
@XmlEnum
public enum MeetingAttendeeType {

    @XmlEnumValue("Organizer")
    ORGANIZER("Organizer"),
    @XmlEnumValue("Required")
    REQUIRED("Required"),
    @XmlEnumValue("Optional")
    OPTIONAL("Optional"),
    @XmlEnumValue("Room")
    ROOM("Room"),
    @XmlEnumValue("Resource")
    RESOURCE("Resource");
    private final String value;

    MeetingAttendeeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MeetingAttendeeType fromValue(String v) {
        for (MeetingAttendeeType c: MeetingAttendeeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
