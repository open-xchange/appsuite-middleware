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

package com.openexchange.ajax.framework;

import java.io.IOException;
import java.util.TimeZone;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.session.LoginRequest;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.LogoutRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.tools.servlet.AjaxException;

/**
 * This class implements the temporary memory of an AJAX client and provides
 * some convenience methods to determine user specific values for running some
 * tests more easily.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AJAXClient {

    private final AJAXSession session;

    private int userId = -1;

    private TimeZone timeZone;

    /**
     * Default constructor.
     */
    public AJAXClient(final AJAXSession session) {
        this.session = session;
    }

    public AJAXClient(final User user) throws ConfigurationException,
        AjaxException, IOException, SAXException, JSONException {
        AJAXConfig.init();
        final String login = AJAXConfig.getProperty(user.login);
        final String password = AJAXConfig.getProperty(user.password);
        session = new AJAXSession();
        session.setId(LoginTools.login(session, new LoginRequest(login,
            password)).getSessionId());
    }

    public int getUserId() throws AjaxException, IOException, SAXException,
        JSONException {
        if (-1 == userId) {
            userId = ConfigTools.get(session, new GetRequest(GetRequest.Tree
                .Identifier)).getId();
        }
        return userId;
    }

    public TimeZone getTimeZone() throws AjaxException, IOException,
        SAXException, JSONException {
        if (null == timeZone) {
            final String tzId = ConfigTools.get(session, new GetRequest(
                GetRequest.Tree.TimeZone)).getString();
            timeZone = TimeZone.getTimeZone(tzId);
        }
        return timeZone;
    }

    public enum User {
        User1(Property.LOGIN, Property.PASSWORD);
        private Property login;
        private Property password;
        private User(final Property login, final Property password) {
            this.login = login;
            this.password = password;
        }
    }

    /**
     * @return the session
     */
    public AJAXSession getSession() {
        return session;
    }

    public void logout() throws AjaxException, IOException, SAXException,
        JSONException {
        if (null != session.getId()) {
            LoginTools.logout(session, new LogoutRequest(session.getId()));
            session.setId(null);
        }
        session.getConversation().clearContents();
    }
}
