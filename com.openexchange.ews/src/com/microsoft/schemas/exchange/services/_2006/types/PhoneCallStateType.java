
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PhoneCallStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PhoneCallStateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Idle"/>
 *     &lt;enumeration value="Connecting"/>
 *     &lt;enumeration value="Alerted"/>
 *     &lt;enumeration value="Connected"/>
 *     &lt;enumeration value="Disconnected"/>
 *     &lt;enumeration value="Incoming"/>
 *     &lt;enumeration value="Transferring"/>
 *     &lt;enumeration value="Forwarding"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PhoneCallStateType")
@XmlEnum
public enum PhoneCallStateType {

    @XmlEnumValue("Idle")
    IDLE("Idle"),
    @XmlEnumValue("Connecting")
    CONNECTING("Connecting"),
    @XmlEnumValue("Alerted")
    ALERTED("Alerted"),
    @XmlEnumValue("Connected")
    CONNECTED("Connected"),
    @XmlEnumValue("Disconnected")
    DISCONNECTED("Disconnected"),
    @XmlEnumValue("Incoming")
    INCOMING("Incoming"),
    @XmlEnumValue("Transferring")
    TRANSFERRING("Transferring"),
    @XmlEnumValue("Forwarding")
    FORWARDING("Forwarding");
    private final String value;

    PhoneCallStateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PhoneCallStateType fromValue(String v) {
        for (PhoneCallStateType c: PhoneCallStateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
