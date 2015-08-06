package com.openexchange.groupware.reminder.osgi;

import com.openexchange.i18n.LocalizableStrings;

final class OAuthScopeDescription implements LocalizableStrings {
    // Application 'xyz' requires following permissions:
    //  - Read reminders for appointments and tasks.
    //  - ...
    public static final String READ_ONLY = "Read reminders for appointments and tasks.";

    // Application 'xyz' requires following permissions:
    //  - Set or change reminders for appointments and tasks.
    //  - ...
    public static final String WRITABLE = "Set or change reminders for appointments and tasks.";
}