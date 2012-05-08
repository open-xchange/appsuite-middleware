
package com.openexchange.spamsettings.generic.service;

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link SpamSettingExceptionMessages} - Exception messages for {@link SpamSettingException} that needs to be translated.
 * 
 * @author francisco.laguna@open-xchange.com
 */
public final class SpamSettingExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // Could not coerce value %1$s into class %2$s
    public static final String COULD_NOT_COERCE_VALUE_MSG = "Could not coerce value %1$s into class %2$s";

    // Can not define metadata %1$s in scope %2$s
    public static final String CAN_NOT_DEFINE_METADATA_MSG = "Can not define metadata %1$s in scope %2$s";

    // Can not set property %1$s in scope %2$s
    public static final String CAN_NOT_SET_PROPERTY_MSG = "Can not set property %1$s in scope %2$s";


    /**
     * Initializes a new {@link SpamSettingExceptionMessages}.
     */
    private SpamSettingExceptionMessages() {
        super();
    }

}
