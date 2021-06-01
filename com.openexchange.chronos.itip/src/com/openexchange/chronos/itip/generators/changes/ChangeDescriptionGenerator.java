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

package com.openexchange.chronos.itip.generators.changes;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link ChangeDescriptionGenerator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ChangeDescriptionGenerator {

    /**
     * An arrays of fields for which human readable {@link Sentence}s can be build
     * 
     * @return An array of processible {@link EventField}s
     */
    public EventField[] getFields();

    /**
     * Generate human readable sentences for specific fields on given diff
     * 
     * @param ctx The {@link Context}
     * @param original The original {@link Event}
     * @param updated The updated {@link Event}
     * @param diff The {@link ITipEventUpdate} of the events
     * @param locale The {@link Locale} to get the human readable sentences in
     * @param timezone The {@link TimeZone} of the user
     * @param recipientUserId The user's Id or '0' if external
     * @return A {@link List} of human readable {@link Sentence} describing the diff
     * @throws OXException In case sentences can't be generated
     * @see #getFields()
     */
    @SuppressWarnings("unused")
    default public List<Sentence> getDescriptions(Context ctx, Event original, Event updated, ITipEventUpdate diff, Locale locale, TimeZone timezone, int recipientUserId) throws OXException {
        return getDescriptions(ctx, original, updated, diff, locale, timezone);
    }

    /**
     * Generate human readable sentences for specific fields on given diff
     * 
     * @param ctx The {@link Context}
     * @param original The original {@link Event}
     * @param updated The updated {@link Event}
     * @param diff The {@link ITipEventUpdate} of the events
     * @param locale The {@link Locale} to get the human readable sentences in
     * @param timezone The {@link TimeZone} of the user
     * @param participantCtxId The user's context Id or '0' if external
     * @param participantUserId The user's Id or '0' if external
     * @return A {@link List} of human readable {@link Sentence} describing the diff
     * @throws OXException In case sentences can't be generated
     * @see #getFields()
     */
    List<Sentence> getDescriptions(Context ctx, Event original, Event updated, ITipEventUpdate diff, Locale locale, TimeZone timezone) throws OXException;
}
