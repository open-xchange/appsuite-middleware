package com.openexchange.tasks.json.osgi;

import com.openexchange.i18n.LocalizableStrings;

final class OAuthScopeDescription implements LocalizableStrings {
    // Application 'xyz' requires following permissions:
    //  - Read all your tasks.
    //  - ...
    public static final String READ_ONLY = "Read all your tasks.";

    // Application 'xyz' requires following permissions:
    //  - Create, modify and delete tasks.
    //  - ...
    public static final String WRITABLE = "Create, modify and delete tasks.";
}