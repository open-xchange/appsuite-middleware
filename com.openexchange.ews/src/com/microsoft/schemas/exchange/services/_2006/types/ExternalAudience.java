
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExternalAudience.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ExternalAudience">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Known"/>
 *     &lt;enumeration value="All"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ExternalAudience")
@XmlEnum
public enum ExternalAudience {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Known")
    KNOWN("Known"),
    @XmlEnumValue("All")
    ALL("All");
    private final String value;

    ExternalAudience(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExternalAudience fromValue(String v) {
        for (ExternalAudience c: ExternalAudience.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
