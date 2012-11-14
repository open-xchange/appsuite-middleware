
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompleteActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CompleteActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Commit"/>
 *     &lt;enumeration value="Abandon"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CompleteActionType")
@XmlEnum
public enum CompleteActionType {

    @XmlEnumValue("Commit")
    COMMIT("Commit"),
    @XmlEnumValue("Abandon")
    ABANDON("Abandon");
    private final String value;

    CompleteActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CompleteActionType fromValue(String v) {
        for (CompleteActionType c: CompleteActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
