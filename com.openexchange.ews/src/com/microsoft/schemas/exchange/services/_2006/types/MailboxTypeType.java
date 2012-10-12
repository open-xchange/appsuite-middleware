
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MailboxTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MailboxTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="OneOff"/>
 *     &lt;enumeration value="Mailbox"/>
 *     &lt;enumeration value="PublicDL"/>
 *     &lt;enumeration value="PrivateDL"/>
 *     &lt;enumeration value="Contact"/>
 *     &lt;enumeration value="PublicFolder"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MailboxTypeType")
@XmlEnum
public enum MailboxTypeType {

    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("OneOff")
    ONE_OFF("OneOff"),
    @XmlEnumValue("Mailbox")
    MAILBOX("Mailbox"),
    @XmlEnumValue("PublicDL")
    PUBLIC_DL("PublicDL"),
    @XmlEnumValue("PrivateDL")
    PRIVATE_DL("PrivateDL"),
    @XmlEnumValue("Contact")
    CONTACT("Contact"),
    @XmlEnumValue("PublicFolder")
    PUBLIC_FOLDER("PublicFolder");
    private final String value;

    MailboxTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MailboxTypeType fromValue(String v) {
        for (MailboxTypeType c: MailboxTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
