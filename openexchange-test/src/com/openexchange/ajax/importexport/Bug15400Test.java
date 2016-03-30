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
package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;

/**
 * {@link Bug15400Test}
 *
 * Import drops records that exceed field widths.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug15400Test extends AbstractManagedContactTest {

	/**
	 * Initializes a new {@link Bug15400Test}.
	 *
	 * @param name The test name
	 */
	public Bug15400Test(String name) {
		super(name);
	}

    public void testBug15400() throws Exception {
        /*
         * check import
         */
        String name =
            "Hadschi Halef Omar Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd al Gossarah Hadschi Halef Omar Ben Hadschi Abul " +
            "Abbas Ibn Hadschi D...as War Knapp Und Wird Hier Abgeschnitten"
        ;
        String truncatedName =
            "Hadschi Halef Omar Ben Hadschi Abul Abbas Ibn Hadschi Dawuhd al Gossarah Hadschi Halef Omar Ben Hadschi Abul " +
            "Abbas Ibn Hadschi D"
        ;
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:2.1\n" +
            "N;CHARSET=Windows-1252:" + name + ";;;\n" +
            "END:VCARD"
        ;
        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vCard.getBytes(Charsets.UTF_8)));
        VCardImportResponse importResponse = getClient().execute(importRequest);
        JSONArray data = (JSONArray) importResponse.getData();
        assertTrue("got no data from import request", null != data && 0 < data.length());
        JSONObject jsonObject = data.getJSONObject(0);
        assertNotNull("got no data from import request", jsonObject);
        int objectID = jsonObject.optInt("id");
        assertTrue("got no object id from import request", 0 < objectID);
        Contact importedContact = manager.getAction(folderID, objectID);
        assertEquals(truncatedName, importedContact.getSurName());
    }

}
