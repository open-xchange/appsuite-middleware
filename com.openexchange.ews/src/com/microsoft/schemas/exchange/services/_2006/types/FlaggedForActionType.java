
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FlaggedForActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FlaggedForActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Any"/>
 *     &lt;enumeration value="Call"/>
 *     &lt;enumeration value="DoNotForward"/>
 *     &lt;enumeration value="FollowUp"/>
 *     &lt;enumeration value="FYI"/>
 *     &lt;enumeration value="Forward"/>
 *     &lt;enumeration value="NoResponseNecessary"/>
 *     &lt;enumeration value="Read"/>
 *     &lt;enumeration value="Reply"/>
 *     &lt;enumeration value="ReplyToAll"/>
 *     &lt;enumeration value="Review"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FlaggedForActionType")
@XmlEnum
public enum FlaggedForActionType {

    @XmlEnumValue("Any")
    ANY("Any"),
    @XmlEnumValue("Call")
    CALL("Call"),
    @XmlEnumValue("DoNotForward")
    DO_NOT_FORWARD("DoNotForward"),
    @XmlEnumValue("FollowUp")
    FOLLOW_UP("FollowUp"),
    FYI("FYI"),
    @XmlEnumValue("Forward")
    FORWARD("Forward"),
    @XmlEnumValue("NoResponseNecessary")
    NO_RESPONSE_NECESSARY("NoResponseNecessary"),
    @XmlEnumValue("Read")
    READ("Read"),
    @XmlEnumValue("Reply")
    REPLY("Reply"),
    @XmlEnumValue("ReplyToAll")
    REPLY_TO_ALL("ReplyToAll"),
    @XmlEnumValue("Review")
    REVIEW("Review");
    private final String value;

    FlaggedForActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FlaggedForActionType fromValue(String v) {
        for (FlaggedForActionType c: FlaggedForActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
