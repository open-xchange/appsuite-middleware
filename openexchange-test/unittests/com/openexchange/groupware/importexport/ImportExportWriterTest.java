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

package com.openexchange.groupware.importexport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.writer.ImportExportWriter;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.exception.OXException;

public class ImportExportWriterTest extends TestCase {

	public void testWriteObject() throws JSONException {
		final ImportExportWriter writer = new ImportExportWriter(null);
		final ImportResult result = new ImportResult("1", "3" , new Date() );
		writer.writeObject(result);
		final JSONObject temp = (JSONObject) writer.getObject();
		assertEquals("ID is incorrect" , "1" , temp.get(DataFields.ID) );
		assertEquals("Folder is incorrect" , "3" , temp.get(CommonFields.FOLDER_ID) );
	    assertNull(temp.optJSONArray("warnings"));
    }

	public void testWriteObjects() throws JSONException {
		final ImportExportWriter writer = new ImportExportWriter(null);
		final List<ImportResult> results = Arrays.asList(
				new ImportResult("1", "3" , new Date() ),
				new ImportResult("2", "4", new Date() ) );
		writer.writeObjects(results);
		final JSONArray resArr = (JSONArray) writer.getObject();
		JSONObject temp = resArr.getJSONObject(0);
		assertEquals("ID is incorrect" , "1" , temp.get(DataFields.ID) );
		assertEquals("Folder is incorrect" , "3" , temp.get(CommonFields.FOLDER_ID) );
		temp = resArr.getJSONObject(1);
		assertEquals("ID is incorrect" , "2" , temp.get(DataFields.ID) );
		assertEquals("Folder is incorrect" , "4" , temp.get(CommonFields.FOLDER_ID) );

    }


    public void testWarnings() throws JSONException  {

        OXException exception = OXException.general("EXCEPTION");

        List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        warnings.add(new ConversionWarning(1, "Warning 1"));
        warnings.add(new ConversionWarning(1, "Warning 2"));
        warnings.add(new ConversionWarning(1, "Warning 3"));
        warnings.add(new ConversionWarning(1, "Warning 4"));

        final ImportExportWriter writer = new ImportExportWriter(null);
        final ImportResult result = new ImportResult();
        result.setObjectId("12");
        result.setException(exception);
        result.addWarnings(warnings);

        writer.writeObject(result);
        final JSONObject temp = (JSONObject) writer.getObject();

        assertFalse(temp.isNull("error"));

        JSONArray jsonWarnings = temp.optJSONArray("warnings");

        assertNotNull(jsonWarnings);

        Set<String> expectedStrings = new HashSet<String>(Arrays.asList("Warning 1", "Warning 2", "Warning 3","Warning 4")) ;

        for(int i = 0, size = jsonWarnings.length(); i < size; i++) {
            JSONObject warning = jsonWarnings.getJSONObject(i);
            assertTrue(expectedStrings.remove(warning.getJSONObject("warnings").getJSONArray("error_stack").get(0)));
        }
        assertTrue(expectedStrings.isEmpty());
    }

}
