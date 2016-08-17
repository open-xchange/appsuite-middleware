/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.calendar.itip;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.i18n.I18nService;
import com.openexchange.server.services.I18nServices;

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
        I18nService i18nService = I18nServices.getInstance().getService(locale);
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

    public static String declined(Locale locale, Context ctxt) {
        I18nService i18nService = I18nServices.getInstance().getService(locale);
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
        I18nService i18nService = I18nServices.getInstance().getService(locale);
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
}
