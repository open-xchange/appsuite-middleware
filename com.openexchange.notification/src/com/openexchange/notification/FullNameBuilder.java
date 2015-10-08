
package com.openexchange.notification;

import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;

/**
 * 
 * {@link FullNameBuilder} - Build a localized user name from given- and surname.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class FullNameBuilder {

    /**
     * Build the full name of the user consisting of given name and surname and orders them according to the user's locale.
     * 
     * @param user The user
     * @return the full name of the user
     */
    public static String buildFullName(User user, Translator translator) {
        String givenName = user.getGivenName();
        String surname = user.getSurname();

        return String.format(translator.translate(NotificationStrings.USER_NAME), givenName, surname);
    }

}
