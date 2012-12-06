
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContainmentComparisonType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ContainmentComparisonType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Exact"/>
 *     &lt;enumeration value="IgnoreCase"/>
 *     &lt;enumeration value="IgnoreNonSpacingCharacters"/>
 *     &lt;enumeration value="Loose"/>
 *     &lt;enumeration value="IgnoreCaseAndNonSpacingCharacters"/>
 *     &lt;enumeration value="LooseAndIgnoreCase"/>
 *     &lt;enumeration value="LooseAndIgnoreNonSpace"/>
 *     &lt;enumeration value="LooseAndIgnoreCaseAndIgnoreNonSpace"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ContainmentComparisonType")
@XmlEnum
public enum ContainmentComparisonType {

    @XmlEnumValue("Exact")
    EXACT("Exact"),
    @XmlEnumValue("IgnoreCase")
    IGNORE_CASE("IgnoreCase"),
    @XmlEnumValue("IgnoreNonSpacingCharacters")
    IGNORE_NON_SPACING_CHARACTERS("IgnoreNonSpacingCharacters"),
    @XmlEnumValue("Loose")
    LOOSE("Loose"),
    @XmlEnumValue("IgnoreCaseAndNonSpacingCharacters")
    IGNORE_CASE_AND_NON_SPACING_CHARACTERS("IgnoreCaseAndNonSpacingCharacters"),
    @XmlEnumValue("LooseAndIgnoreCase")
    LOOSE_AND_IGNORE_CASE("LooseAndIgnoreCase"),
    @XmlEnumValue("LooseAndIgnoreNonSpace")
    LOOSE_AND_IGNORE_NON_SPACE("LooseAndIgnoreNonSpace"),
    @XmlEnumValue("LooseAndIgnoreCaseAndIgnoreNonSpace")
    LOOSE_AND_IGNORE_CASE_AND_IGNORE_NON_SPACE("LooseAndIgnoreCaseAndIgnoreNonSpace");
    private final String value;

    ContainmentComparisonType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ContainmentComparisonType fromValue(String v) {
        for (ContainmentComparisonType c: ContainmentComparisonType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
