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

package com.openexchange.xmpp.tigase;

import tigase.conf.ConfigurationException;
import tigase.conf.Configurator;
import tigase.conf.ConfiguratorAbstract;
import tigase.db.TigaseDBException;
import tigase.server.MessageRouter;
import tigase.server.MessageRouterIfc;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.xmpp.XmppExceptionCodes;
import com.openexchange.xmpp.XmppServer;

/**
 * {@link TigaseXmppServer} - The tigase XMPP server.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TigaseXmppServer implements XmppServer {

    private volatile MessageRouterIfc router;

    /**
     * Creates a new {@link TigaseXmppServer}.
     */
    public TigaseXmppServer() {
        super();
    }

    @Override
    public void init() throws OXException {
        try {
            // loadLogManager();
            /*
             * Configuration
             */
            final ConfiguratorAbstract config = new Configurator();
            {
                final ConfigurationService service = TigaseServiceLookup.getService(ConfigurationService.class);
                final String configPath = System.getProperty("openexchange.propdir");
                config.init(new String[] {
                    "config-type","--gen-config-def",
                    "--admins","admin@devel.tigase.org,admin@test-d",
                    "--ssl-container-class","com.openexchange.xmpp.tigase.TrustAllSslContextContainer", //ssl-container-class=tigase.io.SSLContextContainer
                    "--ssl-certs-location",configPath+"/certs/",
                    "--virt-hosts","devel.tigase.org,test-d",
                    "--user-db","mysql",
                    "--user-db-uri","jdbc:mysql://devel-master.netline.de/tigasedb?user=openexchange&password=secret"});
            }

            // config = new ConfiguratorOld(config_file, args);
            config.setName("basic-conf");

            final MessageRouterIfc router = new MessageRouter();

            router.setName("open-xchange-message-router");
            router.setConfig(config);
            router.start();
            this.router = router;
        } catch (final ConfigurationException e) {
            throw XmppExceptionCodes.CONFIG_ERROR.create(e, e.getMessage());
        } catch (final TigaseDBException e) {
            throw XmppExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private void loadLogManager() {
        final ConfigurationService service = TigaseServiceLookup.getService(ConfigurationService.class);
        String logManagerConfig = service.getText("file-logging.properties");
        if (null == logManagerConfig) {
            logManagerConfig = service.getText("logging.properties");
        }
        if (null == logManagerConfig) {
            logManagerConfig = "tigase.level=ALL\n" + "tigase.xml.level=INFO\n"
                 + "handlers=java.util.logging.ConsoleHandler\n"
                 + "java.util.logging.ConsoleHandler.level=ALL\n"
                 + "java.util.logging.ConsoleHandler.formatter=tigase.util.LogFormatter\n";
        }
        ConfiguratorAbstract.loadLogManagerConfig(logManagerConfig);
    }

    @Override
    public void release() {
        final MessageRouterIfc router = this.router;
        if (null != router) {
            router.release();
        }
    }

}
