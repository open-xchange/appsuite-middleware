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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * DataWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class DataWriter {
	
	protected TimeZone timeZone = null;
	
	protected JSONWriter jsonwriter = null;
	
	public void writeParameter(String name, String value) throws JSONException {
		if (value != null && value.length() > 0) {
			jsonwriter.key(name);
			jsonwriter.value(value);
		}
	}
	
	public void writeParameter(String name, int value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(String name, long value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(String name, float value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(String name, boolean value) throws JSONException {
		jsonwriter.key(name);
		jsonwriter.value(value);
	}
	
	public void writeParameter(String name, Date value) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			jsonwriter.value(value.getTime());
		}
	}

	public void writeParameter(String name, Date value, TimeZone timeZone) throws JSONException {
		writeParameter(name, value, value, timeZone);
	}
	
	public void writeParameter(String name, Date value, Date offsetDate, TimeZone timeZone) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			int offset = timeZone.getOffset(offsetDate.getTime());
			jsonwriter.value(value.getTime() + offset);
		}
	}
	
	public void writeParameter(String name, byte[] value) throws JSONException {
		if (value != null) {
			jsonwriter.key(name);
			jsonwriter.value(new String(value));			
		}
	}
	
	public void writeValue(String value) throws JSONException {
       	if (value != null && value.length() > 0) {
			jsonwriter.value(value);
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}

	public void writeValue(int value) throws JSONException {
		jsonwriter.value(value);
	}
	
	public void writeValue(float value) throws JSONException {
		jsonwriter.value(value);
	}

	public void writeValue(long value) throws JSONException {
		jsonwriter.value(value);
	}

	public void writeValue(boolean value) throws JSONException {
		jsonwriter.value(value);
	}
	
	public void writeValue(Date value) throws JSONException {
		if (value != null) {
			jsonwriter.value(value.getTime());
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
	
	public void writeValue(Date value, TimeZone timeZone) throws JSONException {
		writeValue(value, value, timeZone);
	}

	public void writeValue(Date value, Date offsetDate, TimeZone timeZone) throws JSONException {
		int offset = timeZone.getOffset(offsetDate.getTime());
		if (value != null) {
			jsonwriter.value(value.getTime()+offset);
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
	
	public void writeValue(byte[] value) throws JSONException {
		if (value != null) {
			jsonwriter.value(new String(value));
		} else {
			jsonwriter.value(JSONObject.NULL);
		}
	}
}
