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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link ITipAnalyzerService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public interface ITipAnalyzerService {

    /**
     * Analyzes the given iCal file.
     * 
     * @param ical The iCal file as {@link InputStream}. Stream does <b>not</b> get closed
     * @param format The format to use. <code>html</code> for a {@link com.openexchange.chronos.itip.generators.HTMLWrapper}, else a {@link com.openexchange.chronos.itip.generators.PassthroughWrapper} is used
     * @param session The {@link CalendarSession}
     * @param mailHeader Mail header key-value pairs. Can influence the analyzer to use, if special handling for some clients are necessary, etc.
     * @return {@link List} of {@link ITipAnalysis} containing the analyzed events.
     * @throws OXException Various. DB, access, permission, etc.
     */
    public List<ITipAnalysis> analyze(InputStream ical, String format, CalendarSession session, Map<String, String> mailHeader) throws OXException;
}
