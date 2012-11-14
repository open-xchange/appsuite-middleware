
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResponseTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ResponseTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Organizer"/>
 *     &lt;enumeration value="Tentative"/>
 *     &lt;enumeration value="Accept"/>
 *     &lt;enumeration value="Decline"/>
 *     &lt;enumeration value="NoResponseReceived"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ResponseTypeType")
@XmlEnum
public enum ResponseTypeType {

    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Organizer")
    ORGANIZER("Organizer"),
    @XmlEnumValue("Tentative")
    TENTATIVE("Tentative"),
    @XmlEnumValue("Accept")
    ACCEPT("Accept"),
    @XmlEnumValue("Decline")
    DECLINE("Decline"),
    @XmlEnumValue("NoResponseReceived")
    NO_RESPONSE_RECEIVED("NoResponseReceived");
    private final String value;

    ResponseTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResponseTypeType fromValue(String v) {
        for (ResponseTypeType c: ResponseTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
