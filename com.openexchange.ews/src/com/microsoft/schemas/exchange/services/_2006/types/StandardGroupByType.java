
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StandardGroupByType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="StandardGroupByType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ConversationTopic"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "StandardGroupByType")
@XmlEnum
public enum StandardGroupByType {

    @XmlEnumValue("ConversationTopic")
    CONVERSATION_TOPIC("ConversationTopic");
    private final String value;

    StandardGroupByType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardGroupByType fromValue(String v) {
        for (StandardGroupByType c: StandardGroupByType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
