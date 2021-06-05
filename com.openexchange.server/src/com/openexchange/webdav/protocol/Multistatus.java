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

package com.openexchange.webdav.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Multistatus<T> implements Iterable<WebdavStatus<T>>{

	private final List<WebdavStatus<T>> stadi = new ArrayList<WebdavStatus<T>>();
	private final TIntObjectMap<List<WebdavStatus<T>>> rcMap = new TIntObjectHashMap<List<WebdavStatus<T>>>();

	public Multistatus(){
		super();
	}

    public Multistatus(final WebdavMultistatusException x){
		for(final WebdavProtocolException e : x.getExceptions()) {
		    @SuppressWarnings("unchecked")
            final WebdavStatus<T> status = (WebdavStatus<T>) e;
            addStatus(status);
		}
	}

	public void addStatus(final WebdavStatus<T> status) {
		stadi.add(status);
		List<WebdavStatus<T>> collocated = rcMap.get(status.getStatus());
		if (null == collocated) {
			collocated = new ArrayList<WebdavStatus<T>>();
			rcMap.put(status.getStatus(), collocated);
		}
		collocated.add(status);
	}

	@Override
    public Iterator<WebdavStatus<T>> iterator() {
		return stadi.iterator();
	}

	public Iterable<WebdavStatus<T>> toIterable(final int status) {
		return rcMap.get(status);
	}

	public int[] getStatusCodes(){
		return rcMap.keys();
	}
}
