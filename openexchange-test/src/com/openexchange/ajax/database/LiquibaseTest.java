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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.database;

import static com.openexchange.ajax.framework.AJAXClient.User.User1;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;


/**
 * {@link LiquibaseTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class LiquibaseTest extends AbstractAJAXSession {

    public LiquibaseTest(String name) {
        super(name);
    }

    @Test
    public void testGetCorrectDBTableColumnType() throws IOException {
        String url = client.getProtocol() + "://" + client.getHostname() + "/preliminary/database/v1/configdb/readOnly";

        final PutMethod put = new PutMethod(url);
        String sql = "SHOW CREATE TABLE login2context;";
        put.setRequestEntity(new StringRequestEntity(sql, "text/plain", "UTF-8"));
        put.getHostAuthState().setAuthScheme(new BasicScheme());

        final HttpClient client = new HttpClient();
        setAuth(client);

        boolean loginInfoCorrectType = false;
        try {
            client.executeMethod(put);
            String responseBodyAsString = put.getResponseBodyAsString();
            System.out.println("Response body is: " + responseBodyAsString);
            for (String string : responseBodyAsString.split(",")) {
                if (StringUtils.contains(string, "login_info")) {
                    int indexOf = StringUtils.indexOf(string, "varchar");

                    String tableSizeString = StringUtils.substring(string, indexOf + "varchar(".length(), StringUtils.indexOf(string, ")"));
                    if (tableSizeString.contains("255")) {
                        loginInfoCorrectType = true;

                        // further tests
                        int parseInt = Integer.parseInt(tableSizeString);
                        Assert.assertEquals("Parsed column size of login_info does not match with required 255", parseInt);
                        break;
                    }
                }
            }
        } catch (final Exception x) {
            System.out.println(x.getLocalizedMessage());
        }
        Assert.assertTrue("Did not found column size 255 for 'login_info' within 'SHOW CREATE TABLE login2context'", loginInfoCorrectType);
    }

    private void setAuth(HttpClient client) {
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(User1.getLogin().toString(), User1.getPassword().toString()));
    }
}
