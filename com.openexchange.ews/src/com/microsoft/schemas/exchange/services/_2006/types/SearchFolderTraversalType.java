
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchFolderTraversalType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SearchFolderTraversalType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Shallow"/>
 *     &lt;enumeration value="Deep"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SearchFolderTraversalType")
@XmlEnum
public enum SearchFolderTraversalType {

    @XmlEnumValue("Shallow")
    SHALLOW("Shallow"),
    @XmlEnumValue("Deep")
    DEEP("Deep");
    private final String value;

    SearchFolderTraversalType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SearchFolderTraversalType fromValue(String v) {
        for (SearchFolderTraversalType c: SearchFolderTraversalType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
