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

package com.openexchange.chronos.itip;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;

/**
 * {@link ContextSensitiveMessages}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class ContextSensitiveMessages {

    private static final Logger LOG = LoggerFactory.getLogger(ContextSensitiveMessages.class);

    public enum Context {
        VERB, ADJECTIVE;
    }

    public static String accepted(Locale locale, Context ctxt) {
        I18nService i18nService = getI18Service(locale);
        if (i18nService == null) {
            LOG.debug("No service for {}  found. Using default for bundle ", locale);
            return "accepted";
        }

        switch (ctxt) {
            case VERB:
                // The verb "accepted", like "User A has accepted an appointment."
                return i18nService.getL10NContextLocalized("verb", "accepted");
            case ADJECTIVE:
                // The adjective "accepted", like "The users status is 'accepted'."
                return i18nService.getL10NContextLocalized("adjective", "accepted");
            default:
                return "accepted";
        }
    }

    /**
     * Returns the {@link I18nService} or null if none found
     *
     * @return The {@link I18nService}
     */
    private static I18nService getI18Service(Locale locale) {
        I18nServiceRegistry registry = Services.getOptionalService(I18nServiceRegistry.class);
        if (registry == null) {
            return null;
        }
        return registry.getI18nService(locale);
    }

    public static String declined(Locale locale, Context ctxt) {
        I18nService i18nService = getI18Service(locale);
        if (i18nService == null) {
            LOG.debug("No service for {}  found. Using default for bundle ", locale);
            return "declined";
        }

        switch (ctxt) {
            case VERB:
                // The verb "declined", like "User A has declined an appointment."
                return i18nService.getL10NContextLocalized("verb", "declined");
            case ADJECTIVE:
                // The adjective "declined", like "The users status is 'declined'."
                return i18nService.getL10NContextLocalized("adjective", "declined");
            default:
                return "declined";
        }
    }

    public static String tentative(Locale locale, Context ctxt) {
        I18nService i18nService = getI18Service(locale);
        if (i18nService == null) {
            LOG.debug("No service for {}  found. Using default for bundle ", locale);
            return "tentatively accepted";
        }

        switch (ctxt) {
            case VERB:
                // The verb "tentatively accepted", like "User A has tentatively accepted an appointment."
                return i18nService.getL10NContextLocalized("verb", "tentatively accepted");
            case ADJECTIVE:
                // The adjective "tentatively accepted", like "The users status is 'tentatively accepted'."
                return i18nService.getL10NContextLocalized("adjective", "tentatively accepted");
            default:
                return "tentatively accepted";
        }
    }

    public static String partStat(ParticipationStatus status, Locale locale, Context ctxt) {
        if (ParticipationStatus.ACCEPTED.matches(status)) {
            return accepted(locale, ctxt);
        } else if (ParticipationStatus.DECLINED.matches(status)) {
            return declined(locale, ctxt);
        } else if (ParticipationStatus.TENTATIVE.matches(status)) {
            return tentative(locale, ctxt);
        }
        return status.getValue();
    }

}
