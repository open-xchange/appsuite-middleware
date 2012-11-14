
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeliverMeetingRequestsType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeliverMeetingRequestsType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DelegatesOnly"/>
 *     &lt;enumeration value="DelegatesAndMe"/>
 *     &lt;enumeration value="DelegatesAndSendInformationToMe"/>
 *     &lt;enumeration value="NoForward"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DeliverMeetingRequestsType")
@XmlEnum
public enum DeliverMeetingRequestsType {

    @XmlEnumValue("DelegatesOnly")
    DELEGATES_ONLY("DelegatesOnly"),
    @XmlEnumValue("DelegatesAndMe")
    DELEGATES_AND_ME("DelegatesAndMe"),
    @XmlEnumValue("DelegatesAndSendInformationToMe")
    DELEGATES_AND_SEND_INFORMATION_TO_ME("DelegatesAndSendInformationToMe"),
    @XmlEnumValue("NoForward")
    NO_FORWARD("NoForward");
    private final String value;

    DeliverMeetingRequestsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeliverMeetingRequestsType fromValue(String v) {
        for (DeliverMeetingRequestsType c: DeliverMeetingRequestsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
