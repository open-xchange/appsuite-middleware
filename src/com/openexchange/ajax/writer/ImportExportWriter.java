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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.List;

import org.json.JSONException;

import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.json.OXJSONWriter;

/**
 * ImportExportWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.org">Tobias Prinz</a> (Refactoring comment and errorhandling workaround)
 */

public class ImportExportWriter extends DataWriter {
	
	public ImportExportWriter() {
		jsonwriter = new OXJSONWriter();
	}
	
	public ImportExportWriter(OXJSONWriter jsonwriter) {
		this.jsonwriter = jsonwriter;
	}
	
	public void writeObject(ImportResult importResult) throws JSONException {
		jsonwriter.object();
		writeParameter(DataFields.ID, importResult.getObjectId());
		writeParameter(DataFields.LAST_MODIFIED, importResult.getDate());
		writeParameter(CommonFields.FOLDER_ID, importResult.getFolder());
		
		if (importResult.hasError()) {
			OXException exception = importResult.getException();
			writeParameter("error", exception.getOrigMessage());
	        if (exception.getMessageArgs() != null) {
	        	jsonwriter.key("error_params");
	        	jsonwriter.array();
	            for (Object tmp : exception.getMessageArgs()) {
	            	jsonwriter.value(tmp);
	            }
	            jsonwriter.endArray();
	        }
	        writeParameter("category", exception.getCategory().getCode());
	        writeParameter("code", exception.getErrorCode());
	        writeParameter("error_id", exception.getExceptionID());
	        writeParameter("entry_number", importResult.getEntryNumber());
		}
		jsonwriter.endObject();
	}
	
	
	
	public String toString(){
		return getObject().toString();
	}
	
	public Object getObject(){
		return ((OXJSONWriter) jsonwriter).getObject();
	}

	public void writeObjects(List<ImportResult> importResult) throws JSONException {
		jsonwriter.array();
		for (int a = 0; a < importResult.size(); a++) {
			writeObject(importResult.get(a));
		}
		jsonwriter.endArray();
	}
}
