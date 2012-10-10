
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SyncFolderItemsScopeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SyncFolderItemsScopeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NormalItems"/>
 *     &lt;enumeration value="NormalAndAssociatedItems"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SyncFolderItemsScopeType")
@XmlEnum
public enum SyncFolderItemsScopeType {

    @XmlEnumValue("NormalItems")
    NORMAL_ITEMS("NormalItems"),
    @XmlEnumValue("NormalAndAssociatedItems")
    NORMAL_AND_ASSOCIATED_ITEMS("NormalAndAssociatedItems");
    private final String value;

    SyncFolderItemsScopeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SyncFolderItemsScopeType fromValue(String v) {
        for (SyncFolderItemsScopeType c: SyncFolderItemsScopeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
