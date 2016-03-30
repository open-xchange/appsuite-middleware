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
package com.openexchange.loxandra.json;

import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.writer.CommonWriter;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.loxandra.dto.EAVContact;
import com.openexchange.loxandra.helpers.EAVContactHelper;
import com.openexchange.tools.TimeZoneUtils;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public class EAVContactWriter extends CommonWriter {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EAVContactWriter.class);

	private final TimeZone utc;

	/**
	 * Default Constructor
	 * @param tz
	 */
	public EAVContactWriter(TimeZone tz) {
		super(tz, null);
        utc = TimeZoneUtils.getTimeZone("utc");
	}

	/**
	 * Deserialze an EAVContact to JSON
	 * @param c EAVContact
	 * @param j JSONObject
	 * @throws JSONException
	 */
	public void writeContact(final EAVContact c, final JSONObject j) throws JSONException {
		for (final int column : Contact.JSON_COLUMNS) {

			final ContactField field = ContactField.getByValue(column);
			if (field != null && field.isDBField()) {
				final String key = field.getAjaxName();
				try {
					if (c.contains(column)) {
						if (EAVContactHelper.isNonString(column)) {
							Date d = (Date)c.get(column);
							writeParameter(key, d.getTime(), j);
						} else {
                            writeParameter(key, (String)c.get(column), j);
                        }
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		if (c.getFolderUUIDs().size() != 0) {
            writeParameter("folderUUID", c.getFolderUUIDs().get(0).toString(), j);
        }

		if (c.getUUID() != null) {
            writeParameter("uuid", c.getUUID().toString(), j);
        }

		if (c.getTimeUUID() != null) {
            writeParameter("timeuuid", c.getTimeUUID().toString(), j);
        }

		JSONObject unnamedJson = new JSONObject();
		Iterator<String> iter = c.getUnnamedPropertyNames().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			unnamedJson.put(key, c.getUnnamedProperty(key));
		}
		j.put("unnamed", unnamedJson);
	}
}
