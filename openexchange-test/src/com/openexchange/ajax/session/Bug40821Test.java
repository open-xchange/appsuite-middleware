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

package com.openexchange.ajax.session;

import org.json.JSONObject;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;


/**
 * {@link Bug40821Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class Bug40821Test extends AbstractAJAXSession {

    private String login;
    private String password;
    private AJAXClient clientToLogin;
    private String language;

    public Bug40821Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
        clientToLogin = new AJAXClient(new AJAXSession(), true);

        GetRequest getRequest = new GetRequest(Tree.Language);
        GetResponse getResponse = client.execute(getRequest);
        language = getResponse.getString();
    }

    @Override
    public void tearDown() throws Exception {
        SetRequest setRequest = new SetRequest(Tree.Language, language);
        client.execute(setRequest);
        super.tearDown();
    }

    public void testLoginWithStoreLanguage() throws Exception {
        String languageToSet = "de_DE".equalsIgnoreCase(language) ? "en_US" : "de_DE";
        LoginRequest req = new LoginRequest(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, languageToSet, true, false);
        LoginResponse resp = clientToLogin.execute(req);
        clientToLogin.logout();
        JSONObject json = (JSONObject) resp.getData();
        String locale = json.getString("locale");
        assertEquals("Language was not stored.", languageToSet, locale);
    }

    public void testLoginWithoutStoreLanguage() throws Exception {
        String languageToSet = "de_DE".equalsIgnoreCase(language) ? "en_US" : "de_DE";
        LoginRequest req = new LoginRequest(login, password, LoginTools.generateAuthId(), AJAXClient.class.getName(), AJAXClient.VERSION, languageToSet, false, false);
        LoginResponse resp = clientToLogin.execute(req);
        clientToLogin.logout();
        JSONObject json = (JSONObject) resp.getData();
        String locale = json.getString("locale");
        assertEquals("Language was stored.", language, locale);
    }

}
