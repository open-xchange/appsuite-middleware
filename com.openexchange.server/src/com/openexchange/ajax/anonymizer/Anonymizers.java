/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.anonymizer;

import java.util.Locale;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;


/**
 * {@link Anonymizers} - Utility class for anonymization.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class Anonymizers {

    private static class EmptyAnonymizer<E> implements AnonymizerService<E> {

        EmptyAnonymizer() {
            super();
        }

        @Override
        public Module getModule() {
            return null;
        }

        @Override
        public E anonymize(E entity, Session session) throws OXException {
            return entity;
        }

    }

    private static final EmptyAnonymizer<?> EMPTY_ANONYMIZER = new EmptyAnonymizer<Object>();

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link Anonymizers}.
     */
    private Anonymizers() {
        super();
    }

    /**
     * Checks if specified session denotes a guest user.
     *
     * @param session The session
     * @return <code>true</code> if specified session denotes a guest user; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean isGuest(Session session) throws OXException {
        return null != session && UserStorage.getInstance().isGuest(session.getUserId(), session.getContextId());
    }

    /**
     * Checks if specified session denotes the context administrator.
     *
     * @param session The session
     * @return <code>true</code> if specified user entity identifier denotes the context administrator; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean isAdminUser(Session session) throws OXException {
        return ContextStorage.getStorageContext(session.getContextId()).getMailadmin() == session.getUserId();
    }

    /**
     * Gets all users that shared something to specified guest.
     * <p/>
     * More concrete, gets the identifiers of all other user entities present in the permissions of all shared folders and items the
     * guest user has access to, i.e. the IDs of those users the guest user is allowed to to "see".
     *
     * @param contextID The context identifier
     * @param guestID The guest identifier
     * @return The identifiers from sharing users, or an empty set if there are none
     */
    public static Set<Integer> getSharingUsersFor(int contextID, int guestID) throws OXException {
        return ServerServiceRegistry.getServize(ShareService.class).getSharingUsersFor(contextID, guestID);
    }

    /**
     * Checks if specified user entity identifier is a non-visible guest user for session-associated user.
     *
     * @param possibleGuestId The identifier of the user entity to check
     * @param session The session to check for
     * @return <code>true</code> if specified user entity identifier is a non-visible guest; otherwise <code>false</code>
     * @throws OXException If non-visibility cannot be checked
     */
    public static boolean isNonVisibleGuest(int possibleGuestId, Session session) throws OXException {
        if (null == session) {
            // Unable to check
            return false;
        }

        if (isAdminUser(session)) {
            // Context administrator may see all users/guests
            return false;
        }

        if (false == UserStorage.getInstance().isGuest(possibleGuestId, session.getContextId())) {
            // Provided user entity identifier does not point to a guest
            return false;
        }

        return false == ServerServiceRegistry.getServize(ShareService.class).isGuestVisibleTo(possibleGuestId, session);
    }

    /**
     * Gets the empty anonymizer
     *
     * @return The empty anonymizer
     */
    @SuppressWarnings("unchecked")
    public static <E> AnonymizerService<E> emptyAnonymizerFor() {
        return (AnonymizerService<E>) EMPTY_ANONYMIZER;
    }

    /**
     * Gets the anonymizer for given module.
     *
     * @param module The module
     * @return The appropriate anonymizer or {@link #emptyAnonymizerFor() the empty anonymizer instance}
     * @throws OXException If an anonymizer cannot be returned
     */
    public static <E> AnonymizerService<E> optAnonymizerFor(Module module) throws OXException {
        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            return emptyAnonymizerFor();
        }

        AnonymizerService<E> anonymizer = registry.getAnonymizerFor(module);
        return null == anonymizer ? Anonymizers.<E> emptyAnonymizerFor() : anonymizer;
    }

    /**
     * Optionally anonymizes given entity.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The eventually anonymized entity
     * @throws OXException If a fatal error occurs
     */
    public static <E> E optAnonymize(E entity, Module module, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        return anonymize0(entity, module, session, false);
    }

    /**
     * Optionally anonymizes given entity only if specified session denotes a guest user.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The eventually anonymized entity
     * @throws OXException If a fatal error occurs
     */
    public static <E> E optAnonymizeIfGuest(E entity, Module module, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        if (false == isGuest(session)) {
            return entity;
        }

        return anonymize0(entity, module, session, false);
    }

    /**
     * (Forced) Anonymization for given entity.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The anonymized entity
     * @throws OXException If entity cannot be anonymized
     */
    public static <E> E anonymize(E entity, Module module, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        return anonymize0(entity, module, session, true);
    }

    /**
     * (Forced) Anonymization for given entity only if specified session denotes a guest user.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The anonymized entity or the entity as-is if specified session does not denote a guest user
     * @throws OXException If entity cannot be anonymized
     */
    public static <E> E anonymizeIfGuest(E entity, Module module, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        if (false == isGuest(session)) {
            return entity;
        }

        return anonymize0(entity, module, session, true);
    }

    /**
     * Anonymization for given entity only if specified session denotes a guest user.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @param failOnAbsence <code>true</code> to throw an exception if appropriate resources are absent; otherwise <code>false</code>
     * @return The possibly anonymized entity
     * @throws OXException If entity cannot be anonymized
     */
    private static <E> E anonymize0(E entity, Module module, Session session, boolean failOnAbsence) throws OXException {
        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            if (failOnAbsence) {
                throw ServiceExceptionCode.absentService(AnonymizerRegistryService.class);
            }
            return entity;
        }

        AnonymizerService<E> anonymizer = registry.getAnonymizerFor(module);
        if (null == anonymizer) {
            if (failOnAbsence) {
                throw AnonymizeExceptionCodes.NO_SUCH_ANONYMIZER.create(module.getName());
            }
            return entity;
        }

        return anonymizer.anonymize(entity, session);
    }

    // -----------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the translated user module identifier for given locale.
     *
     * @param string The string to translate
     * @param session The session to determine the locale from
     * @return The translated user module identifier
     * @throws OXException If the translated cannot be returned
     */
    public static String getUserI18nFor(Session session) throws OXException {
        return getI18nFor(AnonymizerStrings.MODULE_USER, session);
    }

    /**
     * Gets the translated group module identifier for given locale.
     *
     * @param string The string to translate
     * @param session The session to determine the locale from
     * @return The translated group module identifier
     * @throws OXException If the translated cannot be returned
     */
    public static String getGroupI18nFor(Session session) throws OXException {
        return getI18nFor(AnonymizerStrings.MODULE_GROUP, session);
    }

    /**
     * Gets the translated resource module identifier for given locale.
     *
     * @param string The string to translate
     * @param session The session to determine the locale from
     * @return The translated resource module identifier
     * @throws OXException If the translated cannot be returned
     */
    public static String getResourceI18nFor(Session session) throws OXException {
        return getI18nFor(AnonymizerStrings.MODULE_RESOURCE, session);
    }

    /**
     * Gets the translated string for given locale.
     *
     * @param string The string to translate
     * @param session The session to determine the locale from
     * @return The translated string
     * @throws OXException If the translated cannot be returned
     */
    public static String getI18nFor(String string, Session session) throws OXException {
        if (null == string) {
            return string;
        }

        return StringHelper.valueOf(getLocaleFrom(session)).getString(string);
    }

    /**
     * Gets the the locale associated with session's user.
     *
     * @param session The session
     * @return The locale
     * @throws OXException If locale cannot be returned
     */
    public static Locale getLocaleFrom(Session session) throws OXException {
        if (null == session) {
            return Locale.US;
        }
        Locale locale = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
        return null == locale ? Locale.US : locale;
    }

}
