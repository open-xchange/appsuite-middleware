/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.writer;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONWriter;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.SearchIterator;

public abstract class TimedWriter<T> {

	protected JSONWriter jsonWriter;

	public TimedWriter(final JSONWriter w){
		jsonWriter = w;
	}

	public void timedResult(final long timestamp) throws JSONException {
		jsonWriter.object();
		jsonWriter.key(ResponseFields.TIMESTAMP);
		jsonWriter.value(timestamp);
		jsonWriter.key(ResponseFields.DATA);
	}

	public void endTimedResult() throws JSONException {
		jsonWriter.endObject();
	}

	public void writeDelta(final SearchIterator<T> iterator, final SearchIterator<T> deleted, final Object[] cols, final boolean ignoreDeleted, final TimeZone tz) throws JSONException, OXException {
		jsonWriter.array();
		fillArray(iterator,cols, tz);
		while (deleted.hasNext() && !ignoreDeleted) {
			final int del = getId(deleted.next());
			jsonWriter.value(del);
		}
		jsonWriter.endArray();
	}

	protected abstract int getId(Object object);

	protected abstract void fillArray(SearchIterator<T> iterator, Object[] cols, TimeZone tz) throws JSONException, OXException;
}
