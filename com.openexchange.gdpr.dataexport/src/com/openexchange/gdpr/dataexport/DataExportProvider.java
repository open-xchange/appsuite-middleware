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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link DataExportProvider} - Writes certain user-associated data to given sink.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@Service
public interface DataExportProvider {

    /**
     * Gets the unique identifier for this provider, which is equal to module's identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks given task arguments.
     *
     * @param args The arguments to check
     * @param session The session providing user information
     * @return <code>true</code> if this provider is applicable according to passed arguments; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean checkArguments(DataExportArguments args, Session session) throws OXException;

    /**
     * Gets the module representation for specified user.
     *
     * @param session The session providing user information
     * @return The optional module
     * @throws OXException If module cannot be returned
     */
    Optional<Module> getModule(Session session) throws OXException;

    /**
     * Gets the path prefix from this provider for specified locale; e.g. <code>"mails"</code>.
     *
     * @param locale The locale
     * @return The path prefix
     * @throws OXException If path prefix cannot be returned
     */
    String getPathPrefix(Locale locale) throws OXException;

    /**
     * Exports this provider's data to given sink.
     *
     * @param processingId A unique identifier for provider's processing used to identify this invocation later on
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @return The export result <code>true</code> if completed; otherwise <code>false</code> if export terminated prematurely (e.g. thread has been interrupted)
     * @throws OXException If export fails
     * @throws InterruptedException If processing thread has been interrupted
     */
    ExportResult export(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException, InterruptedException;

    /**
     * Instructs this provider to pause (if possible) a currently ongoing data export.
     *
     * @param processingId The identifier for provider's processing as previously passed to {@link #export(DataExportSink, Optional, int, int) export()} invocation
     * @param sink The sink for setting a save-point if appropriate; otherwise revoke
     * @param task The data export task
     * @return The pause result possibly providing a save-point
     * @throws OXException If pausing fails
     */
    PauseResult pause(UUID processingId, DataExportSink sink, DataExportTask task) throws OXException;

}
