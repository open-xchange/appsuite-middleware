
package com.openexchange.notification;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.notification.osgi.Services;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.user.UserService;

/**
 *
 * {@link FullNameBuilder} - Build a localized user name from given- and surname.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class FullNameBuilder implements FullNameBuilderService {

    private static final FullNameBuilder INSTANCE = new FullNameBuilder();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static FullNameBuilder getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link FullNameBuilder}.
     */
    private FullNameBuilder() {
        super();
    }

    @Override
    public String buildFullName(int userId, int contextId, Translator translator) throws OXException {
        UserService userService = Services.getOptionalService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        User user = userService.getUser(userId, contextId);
        return buildFullName(user, translator);
    }

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
