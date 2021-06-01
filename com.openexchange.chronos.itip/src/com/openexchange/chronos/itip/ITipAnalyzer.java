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

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * An {@link ITipAnalyzer} knows how to assemble and represent all relevant data about an iTip Message
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ITipAnalyzer {

    /**
     * The methods the analyzer is capable of handling
     * 
     * @return A {@link List} of {@link ITipMethod} that can be handled.
     */
    public List<ITipMethod> getMethods();

    /**
     * Analyzes an {@link ITipMessage} for specific {@link ITipMethod}s. See {@link #getMethods()}.
     * 
     * @param message The {@link ITipMessage} to analyze
     * @param header Mail header key-value pairs. Can influence the analyzer to use, if special handling for some clients are necessary, etc.
     * @param format The format to use. <code>html</code> for a {@link com.openexchange.chronos.itip.generators.HTMLWrapper}, else a {@link com.openexchange.chronos.itip.generators.PassthroughWrapper} is used
     * @param session The {@link CalendarSession}
     * @return An {@link ITipAnalysis} for the given message
     * @throws OXException Various
     */
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, String format, CalendarSession session) throws OXException;
}
