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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.dav.caldav;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.openexchange.dav.caldav.ical.ICalUtils;
import com.openexchange.dav.caldav.ical.SimpleICal;
import com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException;

/**
 * {@link ICalResource}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ICalResource {
	
	private String eTag;
	private String href;
	private final SimpleICal iCal;
	
	public ICalResource(String iCalString, String href, String eTag) throws IOException, SimpleICalException {
		super();
		this.iCal = new SimpleICal(iCalString);
		this.href = href;
		this.eTag = eTag;
	}
	
	public ICalResource(String iCalString) throws IOException, com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException {
		this(iCalString, null, null);
	}
	
	public String getUID() {
		return this.iCal.getVEvent().getPropertyValue("UID");
	}	

	public String getSummary() {
		return this.iCal.getVEvent().getPropertyValue("SUMMARY");
	}	

	public Date getDTStart() throws ParseException {
		return ICalUtils.parseDate(this.iCal.getVEvent().getProperty("DTSTART"));
	}	

	public Date getDTEnd() throws ParseException {
		return ICalUtils.parseDate(this.iCal.getVEvent().getProperty("DTEND"));
	}	

	public String getLocation() {
		return this.iCal.getVEvent().getPropertyValue("LOCATION");
	}
	
	/**
	 * @return the eTag
	 */
	public String getETag() {
		return eTag;
	}

	public void setEtag(String eTag) {
		this.eTag = eTag;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the iCal file
	 */
	public SimpleICal getICal() {
		return iCal;
	}

	@Override
    public String toString() {
		return this.iCal.toString();		
	}
	
}
