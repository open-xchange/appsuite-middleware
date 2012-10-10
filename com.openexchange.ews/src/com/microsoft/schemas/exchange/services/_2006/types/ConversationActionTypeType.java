
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConversationActionTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConversationActionTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AlwaysCategorize"/>
 *     &lt;enumeration value="AlwaysDelete"/>
 *     &lt;enumeration value="AlwaysMove"/>
 *     &lt;enumeration value="Delete"/>
 *     &lt;enumeration value="Move"/>
 *     &lt;enumeration value="Copy"/>
 *     &lt;enumeration value="SetReadState"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConversationActionTypeType")
@XmlEnum
public enum ConversationActionTypeType {

    @XmlEnumValue("AlwaysCategorize")
    ALWAYS_CATEGORIZE("AlwaysCategorize"),
    @XmlEnumValue("AlwaysDelete")
    ALWAYS_DELETE("AlwaysDelete"),
    @XmlEnumValue("AlwaysMove")
    ALWAYS_MOVE("AlwaysMove"),
    @XmlEnumValue("Delete")
    DELETE("Delete"),
    @XmlEnumValue("Move")
    MOVE("Move"),
    @XmlEnumValue("Copy")
    COPY("Copy"),
    @XmlEnumValue("SetReadState")
    SET_READ_STATE("SetReadState");
    private final String value;

    ConversationActionTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConversationActionTypeType fromValue(String v) {
        for (ConversationActionTypeType c: ConversationActionTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
