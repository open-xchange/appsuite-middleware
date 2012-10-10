
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IdFormatType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="IdFormatType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EwsLegacyId"/>
 *     &lt;enumeration value="EwsId"/>
 *     &lt;enumeration value="EntryId"/>
 *     &lt;enumeration value="HexEntryId"/>
 *     &lt;enumeration value="StoreId"/>
 *     &lt;enumeration value="OwaId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "IdFormatType")
@XmlEnum
public enum IdFormatType {

    @XmlEnumValue("EwsLegacyId")
    EWS_LEGACY_ID("EwsLegacyId"),
    @XmlEnumValue("EwsId")
    EWS_ID("EwsId"),
    @XmlEnumValue("EntryId")
    ENTRY_ID("EntryId"),
    @XmlEnumValue("HexEntryId")
    HEX_ENTRY_ID("HexEntryId"),
    @XmlEnumValue("StoreId")
    STORE_ID("StoreId"),
    @XmlEnumValue("OwaId")
    OWA_ID("OwaId");
    private final String value;

    IdFormatType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IdFormatType fromValue(String v) {
        for (IdFormatType c: IdFormatType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
