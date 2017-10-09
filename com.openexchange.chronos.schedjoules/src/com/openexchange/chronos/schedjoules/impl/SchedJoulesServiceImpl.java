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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.impl;

import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.schedjoules.SchedJoulesResult;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link SchedJoulesServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesServiceImpl implements SchedJoulesService {

    /**
     * Default 'X-WR-CALNAME' and 'SUMMARY' contents of an iCal that is not accessible
     */
    private static final String NO_ACCESS = "You have no access to this calendar";

    private final SchedJoulesAPI api;

    /**
     * Initialises a new {@link SchedJoulesServiceImpl}.
     * 
     * @throws OXException if the {@link SchedJoulesAPI} cannot be initialised
     */
    public SchedJoulesServiceImpl() throws OXException {
        super();
        api = new SchedJoulesAPI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot()
     */
    @Override
    public SchedJoulesResult getRoot() throws OXException {
        return new SchedJoulesResult(api.pages().getRootPage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot(java.lang.String, java.lang.String)
     */
    @Override
    public SchedJoulesResult getRoot(String locale, String location) throws OXException {
        return new SchedJoulesResult(api.pages().getRootPage(locale, location));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.service.SchedJoulesService#getPage(int)
     */
    @Override
    public SchedJoulesResult getPage(int pageId) throws OXException {
        return new SchedJoulesResult(api.pages().getPage(pageId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getPage(int, java.lang.String)
     */
    @Override
    public SchedJoulesResult getPage(int pageId, String locale) throws OXException {
        return new SchedJoulesResult(api.pages().getPage(pageId, locale));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#subscribeCalendar(int)
     */
    @Override
    public String subscribeCalendar(int id) throws OXException {
        JSONObject page = api.pages().getPage(id);
        if (!page.hasAndNotNull("url")) {
            throw SchedJoulesExceptionCodes.NO_CALENDAR.create(id);
        }

        try {
            String url = page.getString("url");
            URL u = new URL(url);
            Calendar calendar = api.calendar().getCalendar(u);
            if (NO_ACCESS.equals(calendar.getName())) {
                throw SchedJoulesExceptionCodes.NO_ACCESS.create(id);
            }
            //TODO: Hook-up with the SchedJoules provider to subscribe to the calendar
            return calendar.getProdId();
        } catch (JSONException e) {
            throw SchedJoulesExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw SchedJoulesExceptionCodes.INVALID_URL.create(id, e);
        }
    }
}
