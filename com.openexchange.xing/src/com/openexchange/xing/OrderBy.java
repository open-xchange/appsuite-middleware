
package com.openexchange.xing;

/**
 * Provides the supported fields that determines the ascending order of the returned list.
 * <p>
 * Currently only supports "last_name". Defaults to "id".
 */
public enum OrderBy {

    ID(UserField.ID), LAST_NAME(UserField.LAST_NAME);

    private final String fieldName;

    private OrderBy(final UserField userField) {
        this.fieldName = userField.getFieldName();
    }

    public String getFieldName() {
        return fieldName;
    }
}
