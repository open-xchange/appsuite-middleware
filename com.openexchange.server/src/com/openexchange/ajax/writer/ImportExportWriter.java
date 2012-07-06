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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.tools.session.ServerSession;

/**
 * This writer's main objective is to wrap ImportResults into JSON, which then
 * is fed to the AJAX GUI of the OX. TODO remove JSONWriter
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.org">Tobias Prinz</a>
 *         (Refactoring comment and errorhandling workaround)
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImportExportWriter extends DataWriter {

    private final ServerSession session;

	/**
	 * Initializes a new {@link ImportExportWriter}
	 */
	public ImportExportWriter(final ServerSession session) {
		this(new OXJSONWriter(), session);
	}

	/**
	 * Initializes a new {@link ImportExportWriter}
	 *
	 * @param jsonwriter
	 *            The JSON writer to write to
	 */
	public ImportExportWriter(final OXJSONWriter jsonwriter, final ServerSession session) {
		super(null, jsonwriter);
		this.session = session;
	}

	public void writeObject(final ImportResult importResult) throws JSONException {
        if (importResult.hasError()) {
            final OXException exception = importResult.getException();
            final JSONObject jsonObject = new JSONObject();
            ResponseWriter.addException(jsonObject, exception, null != session ? session.getUser().getLocale() : null);

            jsonwriter.object();
            writeDepth1(jsonObject);

            final List<ConversionWarning> warnings = importResult.getWarnings();
            if(warnings != null && warnings.size() > 0) {
            	jsonwriter.key("warnings");
                jsonwriter.array();
                for (final ConversionWarning warning : warnings) {
                    jsonwriter.object();
                    final JSONObject jsonWarning = new JSONObject();
                    ResponseWriter.addWarning(jsonWarning, warning, null != session ? session.getUser().getLocale() : null);
                    writeDepth1(jsonWarning);
                    jsonwriter.endObject();
                }
                jsonwriter.endArray();

                writeParameter(DataFields.ID, importResult.getObjectId());
                writeParameter(DataFields.LAST_MODIFIED, importResult.getDate());
                writeParameter(FolderChildFields.FOLDER_ID, importResult.getFolder());
            }
            jsonwriter.endObject();
        } else {
    		jsonwriter.object();
    		writeParameter(DataFields.ID, importResult.getObjectId());
    		writeParameter(DataFields.LAST_MODIFIED, importResult.getDate());
    		writeParameter(FolderChildFields.FOLDER_ID, importResult.getFolder());
    		jsonwriter.endObject();
        }
   }

    private void writeDepth1(final JSONObject json) throws JSONException {
        final Set<Map.Entry<String, Object>> entrySet = json.entrySet();
		final int len = entrySet.size();
		final Iterator<Map.Entry<String, Object>> iter = entrySet.iterator();
		for (int i = 0; i < len; i++) {
			final Map.Entry<String, Object> e = iter.next();
			jsonwriter.key(e.getKey()).value(e.getValue());
		}
    }

    @Override
	public String toString() {
		return getObject().toString();
	}

	public Object getObject() {
		return ((OXJSONWriter) jsonwriter).getObject();
	}

	public void writeObjects(final List<ImportResult> importResult) throws JSONException {
		jsonwriter.array();
		for (int a = 0; a < importResult.size(); a++) {
			writeObject(importResult.get(a));
		}
		jsonwriter.endArray();
	}
}
