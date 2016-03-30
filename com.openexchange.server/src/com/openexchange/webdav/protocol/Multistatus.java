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

package com.openexchange.webdav.protocol;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		if(null == collocated) {
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
