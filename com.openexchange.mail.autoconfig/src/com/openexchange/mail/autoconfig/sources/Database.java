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

package com.openexchange.mail.autoconfig.sources;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.tools.MailValidator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Database}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Database extends AbstractConfigSource {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(Database.class);

    private final ServiceLookup services;

    public Database(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context) throws OXException {
        Autoconfig autoconfig = new Autoconfig();

        DatabaseService database = services.getService(DatabaseService.class);
        Connection connection = database.getReadOnly(context);

        try {
            getStore(connection, autoconfig, emailLocalPart, emailDomain, password);
            String username = autoconfig.getUsername();
            getTransport(connection, autoconfig, emailLocalPart, emailDomain, password);
            if (username != null && !username.equals(autoconfig.getUsername())) {
                return null;
            }
        } catch (SQLException e) {
            LOG.warn("Internal SQL Error", e);
            return null;
        } catch (URISyntaxException e) {
            LOG.warn("Internal Error", e);
            return null;
        } finally {
            database.backReadOnly(context, connection);
        }

        if (autoconfig.getMailServer() == null || autoconfig.getTransportServer() == null) {
            return null;
        }

        return autoconfig;
    }

    private void getTransport(Connection con, Autoconfig autoconfig, String emailLocalPart, String emailDomain, String password) throws SQLException, URISyntaxException {
        String select = "SELECT DISTINCT url, login, send_addr FROM user_transport_account WHERE send_addr LIKE ?";

        PreparedStatement statement = con.prepareStatement(select);
        statement.setString(1, "%" + emailDomain);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String host = null;
            String protocol = null;
            boolean secure = false;
            int port = -1;
            String username;

            String url = rs.getString("url");

            try {

                URI uri = URIParser.parse(url, URIDefaults.SMTP);
                if (uri.getHost() != null) {
                    host = uri.getHost();
                    protocol = URIDefaults.SMTP.getProtocol();
                    secure = uri.getScheme().endsWith("s");
                    if (secure) {
                        port = URIDefaults.SMTP.getSSLPort();
                    } else {
                        port = URIDefaults.SMTP.getPort();
                    }
                }

                if (!MailValidator.checkForSmtp(host, port)) {
                    continue;
                }

                String login = rs.getString("login");
                String sendAddr = rs.getString("send_addr");
                if (login.equals(sendAddr)) {
                    username = emailLocalPart + "@" + emailDomain;
                } else if (login.equals(sendAddr.split("@")[0])) {
                    username = emailLocalPart;
                } else {
                    continue;
                }

                if (!MailValidator.validateSmtp(host, port, username, password)) {
                    continue;
                }
            } catch (Throwable t) {
                // Try next entry, if any problem occurs.
                continue;
            }

            autoconfig.setTransportServer(host);
            autoconfig.setTransportProtocol(protocol);
            autoconfig.setTransportSecure(secure);
            autoconfig.setTransportPort(port);
            autoconfig.setUsername(username);
            break;
        }

        DBUtils.closeSQLStuff(rs, statement);
    }

    private void getStore(Connection con, Autoconfig autoconfig, String emailLocalPart, String emailDomain, String password) throws SQLException, URISyntaxException {
        Autoconfig tempConfig = new Autoconfig();

        String select = "SELECT DISTINCT url, login, primary_addr FROM user_mail_account WHERE primary_addr LIKE ?";

        PreparedStatement statement = con.prepareStatement(select);
        statement.setString(1, "%" + emailDomain);
        ResultSet rs = statement.executeQuery();

        boolean foundImap = false;
        boolean foundPop3 = false;
        while (rs.next() && !foundImap) {
            String host = null;
            String protocol = null;
            boolean secure = false;
            int port = -1;
            String username;

            String url = rs.getString("url");

            URIDefaults uriDefaults;
            if (url.startsWith("imap")) {
                uriDefaults = URIDefaults.IMAP;
            } else if (url.startsWith("pop") && !foundPop3) {
                uriDefaults = URIDefaults.POP3;
            } else {
                continue;
            }

            try {

                URI uri = URIParser.parse(url, uriDefaults);
                if (uri.getHost() != null) {
                    host = uri.getHost();
                    protocol = uriDefaults.getProtocol();
                    secure = uri.getScheme().endsWith("s");
                    if (secure) {
                        port = uriDefaults.getSSLPort();
                    } else {
                        port = uriDefaults.getPort();
                    }
                }

                if (uriDefaults == URIDefaults.IMAP && !MailValidator.checkForImap(host, port)) {
                    continue;
                }
                if (uriDefaults == URIDefaults.POP3 && !MailValidator.checkForPop3(host, port)) {
                    continue;
                }

                String login = rs.getString("login");
                String primaryAddr = rs.getString("primary_addr");
                if (login.equals(primaryAddr)) {
                    username = emailLocalPart + "@" + emailDomain;
                } else if (login.equals(primaryAddr.split("@")[0])) {
                    username = emailLocalPart;
                } else {
                    continue;
                }

                if (uriDefaults == URIDefaults.IMAP) {
                    if (MailValidator.validateImap(host, port, username, password)) {
                        foundImap = true;
                    } else {
                        continue;
                    }
                } else {
                    if (MailValidator.validatePop3(host, port, username, password)) {
                        foundPop3 = true;
                    } else {
                        continue;
                    }
                }
            } catch (Throwable t) {
                // Try next entry, if any problem occurs.
                continue;
            }

            tempConfig.setMailServer(host);
            tempConfig.setMailProtocol(protocol);
            tempConfig.setMailSecure(secure);
            tempConfig.setMailPort(port);
            tempConfig.setUsername(username);
        }

        autoconfig.setMailServer(tempConfig.getMailServer());
        autoconfig.setMailProtocol(tempConfig.getMailProtocol());
        autoconfig.setMailSecure(tempConfig.isTransportSecure());
        autoconfig.setMailPort(tempConfig.getMailPort());
        autoconfig.setUsername(tempConfig.getUsername());

        DBUtils.closeSQLStuff(rs, statement);
    }

}
