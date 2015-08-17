package com.openexchange.contacts.json.osgi;

import com.openexchange.i18n.LocalizableStrings;

final class OAuthScopeDescription implements LocalizableStrings {
    // Application 'xyz' requires following permissions:
    //  - Read all your contacts.
    //  - ...
    public static final String READ_ONLY = "Read all your contacts.";

    // Application 'xyz' requires following permissions:
    //  - Create, modify and delete contacts.
    //  - ...
    public static final String WRITABLE = "Create, modify and delete contacts.";
}