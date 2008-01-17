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

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * DataWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class DataWriter {
	
	protected TimeZone timeZone;
	
	protected JSONWriter jsonwriter;
	
	public void writeParameter(final String name, final String value) throws JSONException {
		if (value != null && value.length() > 0) {
			jsonwriter.key(name);
			jsonwriter.value(value);
		}
	}
	
	public void writeParameter(final String name, final int value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(final String name, final long value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(final String name, final float value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(final String name, final boolean value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(final String name, final Date value) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			jsonwriter.value(value.getTime());
		}
	}

	public void writeParameter(final String name, final Date value, final TimeZone timeZone) throws JSONException {
		writeParameter(name, value, value, timeZone);
	}
	
	public void writeParameter(final String name, final Date value, final Date offsetDate, final TimeZone timeZone) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			final int offset = timeZone.getOffset(offsetDate.getTime());
			jsonwriter.value(value.getTime() + offset);
		}
	}
	
	public void writeParameter(final String name, final byte[] value) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			jsonwriter.value(new String(value));			
		}
	}
	
	public void writeValue(final String value) throws JSONException {
       	if (value != null && value.length() > 0) {
			jsonwriter.value(value);
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}

	public void writeValue(final int value) throws JSONException {
		jsonwriter.value(value);
	}
	
	public void writeValue(final float value) throws JSONException {
		jsonwriter.value(value);
	}

	public void writeValue(final long value) throws JSONException {
		jsonwriter.value(value);
	}

	public void writeValue(final boolean value) throws JSONException {
		jsonwriter.value(value);
	}
	
	public void writeValue(final Date value) throws JSONException {
		if (value != null) {
			jsonwriter.value(value.getTime());
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
	
	public void writeValue(final Date value, final TimeZone timeZone) throws JSONException {
		writeValue(value, value, timeZone);
	}

	public void writeValue(final Date value, final Date offsetDate, final TimeZone timeZone) throws JSONException {
		final int offset = timeZone.getOffset(offsetDate.getTime());
		if (value != null) {
			jsonwriter.value(value.getTime()+offset);
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
	
	public void writeValue(final byte[] value) throws JSONException {
		if (value != null) {
			jsonwriter.value(new String(value));
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
	
	// new implementation
	
	public void writeParameter(final String name, final String value, final JSONObject jsonObj) throws JSONException {
		if (value != null && value.length() > 0) {
			jsonObj.put(name, value);
		}
	}
	
	public void writeParameter(final String name, final int value, final JSONObject jsonObj) throws JSONException {
		jsonObj.put(name, value);
	}
	
	public void writeParameter(final String name, final long value, final JSONObject jsonObj) throws JSONException {
		jsonObj.put(name, value);
	}
	
	public void writeParameter(final String name, final float value, final JSONObject jsonObj) throws JSONException {
		jsonObj.put(name, value);
	}
	
	public void writeParameter(final String name, final boolean value, final JSONObject jsonObj) throws JSONException {
		jsonObj.put(name, value);
	}
	
	public void writeParameter(final String name, final Date value, final JSONObject jsonObj) throws JSONException {
		if (value != null) {
			jsonObj.put(name, value.getTime());
		}
	}

	public void writeParameter(final String name, final Date value, final TimeZone timeZone, final JSONObject jsonObj) throws JSONException {
		writeParameter(name, value, value, timeZone, jsonObj);
	}
	
	public void writeParameter(final String name, final Date value, final Date offsetDate, final TimeZone timeZone,  final JSONObject jsonObj) throws JSONException {
		if (value != null) {
			final int offset = timeZone.getOffset(offsetDate.getTime());
			jsonObj.put(name, value.getTime() + offset);
		}
	}
	
	public void writeParameter(final String name, final byte[] value, final JSONObject jsonObj) throws JSONException {
		if (value != null) {
			jsonObj.put(name, new String(value));
		}
	}
	
	public void writeValue(final String value, final JSONArray jsonArray) throws JSONException {
       	if (value != null && value.length() > 0) {
			jsonArray.put(value);
		} else {
			jsonArray.put(JSONObject.NULL);
		}
	}

	public void writeValue(final int value, final JSONArray jsonArray) throws JSONException {
		jsonArray.put(value);
	}
	
	public void writeValue(final float value, final JSONArray jsonArray) throws JSONException {
		jsonArray.put(value);
	}

	public void writeValue(final long value, final JSONArray jsonArray) throws JSONException {
		jsonArray.put(value);
	}

	public void writeValue(final boolean value, final JSONArray jsonArray) throws JSONException {
		jsonArray.put(value);
	}
	
	public void writeValue(final Date value, final JSONArray jsonArray) throws JSONException {
		if (value != null) {
			jsonArray.put(value.getTime());
		} else {
			jsonArray.put(JSONObject.NULL);
		}
	}
	
	public void writeValue(final Date value, final TimeZone timeZone, final JSONArray jsonArray) throws JSONException {
		writeValue(value, value, timeZone, jsonArray);
	}

	public void writeValue(final Date value, final Date offsetDate, final TimeZone timeZone, final JSONArray jsonArray) throws JSONException {
		if (value != null) {
			final int offset = timeZone.getOffset(offsetDate.getTime());
			jsonArray.put(value.getTime()+offset);
		} else {
			jsonArray.put(JSONObject.NULL);
		}
	}
	
	public void writeValue(final byte[] value, final JSONArray jsonArray) throws JSONException {
		if (value != null) {
			jsonArray.put(new String(value));
		} else {
			jsonArray.put(JSONObject.NULL);
		}
	}
	
	public void writeValueNull(final JSONArray jsonArray) throws JSONException {
		jsonArray.put(JSONObject.NULL);
	}
}
