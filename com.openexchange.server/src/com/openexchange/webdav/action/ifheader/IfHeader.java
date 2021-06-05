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

package com.openexchange.webdav.action.ifheader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IfHeader {

	private final List<IfHeaderList> lists = new ArrayList<IfHeaderList>();
	private final List<IfHeaderList> untagged = new ArrayList<IfHeaderList>();
	private final Map<String, List<IfHeaderList>> tagged = new HashMap<String, List<IfHeaderList>>();

	public List<IfHeaderList> getLists() {
		return Collections.unmodifiableList(lists );
	}

	public List<IfHeaderList> getRelevant(final String url) {
		final List<IfHeaderList> retval = new ArrayList<IfHeaderList>(untagged);
		if (tagged.containsKey(url)) {
			retval.addAll(tagged.get(url));
		}
		return retval;
	}

	public IfHeaderList getList(final int i) {
		return lists.get(i);
	}

	public void addList(final IfHeaderList list) {
		if (list.isTagged()) {
			final String tag = list.getTag();
			List<IfHeaderList> taggedList = tagged.get(tag);
			if (null == taggedList) {
				taggedList = new ArrayList<IfHeaderList>();
				tagged.put(tag, taggedList);
			}
			taggedList.add(list);
		} else {
			untagged.add(list);
		}
		lists.add(list);
	}



}
