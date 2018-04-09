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
     * @param format The format to use. <code>html</code> for a {@link com.openexchange.chronos.itip.generators.HTMLWrapper}, else a {@link com.openexchange.chronos.itip.generators.changes.PassthroughWrapper} is used
     * @param session The {@link CalendarSession}
     * @return An {@link ITipAnalysis} for the given message
     * @throws OXException Various
     */
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, String format, CalendarSession session) throws OXException;
}
