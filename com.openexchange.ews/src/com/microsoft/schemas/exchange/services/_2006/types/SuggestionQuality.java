
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SuggestionQuality.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SuggestionQuality">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Excellent"/>
 *     &lt;enumeration value="Good"/>
 *     &lt;enumeration value="Fair"/>
 *     &lt;enumeration value="Poor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SuggestionQuality")
@XmlEnum
public enum SuggestionQuality {

    @XmlEnumValue("Excellent")
    EXCELLENT("Excellent"),
    @XmlEnumValue("Good")
    GOOD("Good"),
    @XmlEnumValue("Fair")
    FAIR("Fair"),
    @XmlEnumValue("Poor")
    POOR("Poor");
    private final String value;

    SuggestionQuality(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SuggestionQuality fromValue(String v) {
        for (SuggestionQuality c: SuggestionQuality.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
