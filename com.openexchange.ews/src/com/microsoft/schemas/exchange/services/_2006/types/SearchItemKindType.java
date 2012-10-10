
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchItemKindType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SearchItemKindType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Email"/>
 *     &lt;enumeration value="Meetings"/>
 *     &lt;enumeration value="Tasks"/>
 *     &lt;enumeration value="Notes"/>
 *     &lt;enumeration value="Docs"/>
 *     &lt;enumeration value="Journals"/>
 *     &lt;enumeration value="Contacts"/>
 *     &lt;enumeration value="Im"/>
 *     &lt;enumeration value="Voicemail"/>
 *     &lt;enumeration value="Faxes"/>
 *     &lt;enumeration value="Posts"/>
 *     &lt;enumeration value="Rssfeeds"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SearchItemKindType")
@XmlEnum
public enum SearchItemKindType {

    @XmlEnumValue("Email")
    EMAIL("Email"),
    @XmlEnumValue("Meetings")
    MEETINGS("Meetings"),
    @XmlEnumValue("Tasks")
    TASKS("Tasks"),
    @XmlEnumValue("Notes")
    NOTES("Notes"),
    @XmlEnumValue("Docs")
    DOCS("Docs"),
    @XmlEnumValue("Journals")
    JOURNALS("Journals"),
    @XmlEnumValue("Contacts")
    CONTACTS("Contacts"),
    @XmlEnumValue("Im")
    IM("Im"),
    @XmlEnumValue("Voicemail")
    VOICEMAIL("Voicemail"),
    @XmlEnumValue("Faxes")
    FAXES("Faxes"),
    @XmlEnumValue("Posts")
    POSTS("Posts"),
    @XmlEnumValue("Rssfeeds")
    RSSFEEDS("Rssfeeds");
    private final String value;

    SearchItemKindType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SearchItemKindType fromValue(String v) {
        for (SearchItemKindType c: SearchItemKindType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
