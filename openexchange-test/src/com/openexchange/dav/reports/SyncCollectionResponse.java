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

package com.openexchange.dav.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;

/**
 * {@link SyncCollectionResponse} - Custom response to an "sync-collection" report 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionResponse {
	
	private final MultiStatusResponse[] responses;
	private final String syncToken;
	
	public SyncCollectionResponse(MultiStatus multiStatus, String syncToken) {
		super();
		this.responses = multiStatus.getResponses();
		this.syncToken = syncToken;
	}
	
	/**
	 * @return the syncToken
	 */
	public String getSyncToken() {
		return syncToken;
	}

	/**
	 * @return the responses
	 */
	public MultiStatusResponse[] getResponses() {
		return responses;
	}

	public Map<String, String> getETagsStatusOK() {
		Map<String, String> eTags = new HashMap<String, String>();
        for (MultiStatusResponse response : responses) {
        	if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
	        	String href = response.getHref();
	        	Assert.assertNotNull("got no href from response", href);
	        	Object value = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.GETETAG).getValue();
	        	Assert.assertNotNull("got no ETag from response", value);
	        	String eTag = (String)value;
	        	eTags.put(href, eTag);
        	}
		}
		return eTags;
	}
	
	public List<String> getHrefsStatusNotFound() {
		List<String> hrefs = new ArrayList<String>();
        for (MultiStatusResponse response : responses) {
        	if (null != response.getStatus() && 0 < response.getStatus().length && null != response.getStatus()[0] && 
        			StatusCodes.SC_NOT_FOUND == response.getStatus()[0].getStatusCode()) {
            	hrefs.add(response.getHref());
        	}
        }
		return hrefs;
	}
	
}
