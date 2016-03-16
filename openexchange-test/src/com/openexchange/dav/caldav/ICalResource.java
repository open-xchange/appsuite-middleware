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

package com.openexchange.dav.caldav;

import java.io.IOException;
import java.util.List;
import com.openexchange.dav.caldav.ical.ICalUtils;
import com.openexchange.dav.caldav.ical.SimpleICal;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException;

/**
 * {@link ICalResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ICalResource {

    public static final String VFREEBUSY = "VFREEBUSY";
    public static final String VCALENDAR = "VCALENDAR";
    public static final String VEVENT = "VEVENT";
    public static final String VALARM = "VALARM";
    public static final String VTODO = "VTODO";
    public static final String VTIMEZONE = "VTIMEZONE";

    private String eTag;
	private String href;
    private final Component vCalendar;

	public ICalResource(String iCalString, String href, String eTag) throws IOException, SimpleICalException {
		super();
		this.href = href;
		this.eTag = eTag;
		this.vCalendar = SimpleICal.parse(iCalString);
	}

	public ICalResource(String iCalString) throws IOException, com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException {
		this(iCalString, null, null);
	}

    public Component getVEvent() {
        List<Component> components = vCalendar.getComponents(VEVENT);
        return 0 < components.size() ? components.get(0) : null;
    }

    public Component getVTodo() {
        List<Component> components = vCalendar.getComponents(VTODO);
        return 0 < components.size() ? components.get(0) : null;
    }

	public Component getVFreeBusy() {
	    List<Component> components = vCalendar.getComponents(VFREEBUSY);
	    return 0 < components.size() ? components.get(0) : null;
    }

	public List<Component> getVEvents() {
	    return vCalendar.getComponents(VEVENT);
    }

	public List<Component> getVFreeBusys() {
	    return vCalendar.getComponents(VFREEBUSY);
	}

	public void addComponent(Component component) {
	    vCalendar.getComponents().add(component);
	}

	@Override
	public String toString() {
	    return ICalUtils.fold(this.vCalendar.toString());
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

}
