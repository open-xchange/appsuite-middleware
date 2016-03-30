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

package com.openexchange.groupware.tools.mappings.json;

import java.util.Arrays;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.ArrayFactory;
import com.openexchange.session.Session;

/**
 * {@link ArrayMapping} - JSON specific mapping for array properties.
 *
 * @param <O> the type of the object
 * @param <T> the type of the array elements
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ArrayMapping<T, O> extends DefaultJsonMapping<T[], O> implements ArrayFactory<T> {

	public ArrayMapping(final String ajaxName, final int columnID) {
		super(ajaxName, columnID);
	}

	protected abstract T deserialize(final JSONArray array, int index) throws JSONException, OXException;

	@Override
	public void deserialize(JSONObject from, O to) throws JSONException, OXException {
		if (from.isNull(getAjaxName())) {
			this.set(to, null);
		} else {
			JSONArray jsonArray = from.getJSONArray(getAjaxName());
			int size = jsonArray.length();
			T[] array = newArray(size);
			for (int i = 0; i < size; i++) {
				array[i] = this.deserialize(jsonArray, i);
			}
			this.set(to, array);
		}
	}

	@Override
	public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
		final T[] value = this.get(from);
		return null != value ? new JSONArray(Arrays.asList(value)) : (JSONArray)null;
	}

}
