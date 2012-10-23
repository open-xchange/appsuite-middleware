
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SubscriptionStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SubscriptionStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OK"/>
 *     &lt;enumeration value="Unsubscribe"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SubscriptionStatusType")
@XmlEnum
public enum SubscriptionStatusType {

    OK("OK"),
    @XmlEnumValue("Unsubscribe")
    UNSUBSCRIBE("Unsubscribe");
    private final String value;

    SubscriptionStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SubscriptionStatusType fromValue(String v) {
        for (SubscriptionStatusType c: SubscriptionStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
