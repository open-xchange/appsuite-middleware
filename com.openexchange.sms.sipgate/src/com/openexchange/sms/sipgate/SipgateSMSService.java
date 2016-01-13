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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.sms.sipgate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.TypeFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sms.SMSService;

/**
 * {@link SipgateSMSService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class SipgateSMSService implements SMSService, Reloadable {

    private final ServiceLookup services;
    private final XmlRpcClient client;

    /**
     * Initializes a new {@link SipgateSMSService}.
     *
     * @throws OXException
     */
    public SipgateSMSService(ServiceLookup services) throws OXException {
        super();
        this.services = services;
        ConfigurationService configService = services.getService(ConfigurationService.class);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        String sipgateUsername = configService.getProperty("com.openexchange.sms.sipgate.username");
        String sipgatePassword = configService.getProperty("com.openexchange.sms.sipgate.password");
        try {
            config.setServerURL(new URL("https://api.sipgate.net/my/xmlrpcfacade/"));
        } catch (MalformedURLException e) {
            // will not happen
        }
        config.setBasicUserName(sipgateUsername);
        config.setBasicPassword(sipgatePassword);
        XmlRpcClient client = new XmlRpcClient();
        TypeFactory typeFactory = new SipgateTypeFactory(client);
        client.setTypeFactory(typeFactory);
        client.setConfig(config);
        this.client = client;

    }

    @Override
    public void sendMessage(String recipient, String message) throws OXException {
        try {
            Vector<Object> params = new Vector<>();
            params.addElement("sip:" + recipient + "@sipgate.net");
            params.addElement("text");
            params.addElement(message);
            Object resp = client.execute("samurai.SessionInitiate", params);
        } catch (XmlRpcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String[] recipients, String message) throws OXException {
        String[] recipient = new String[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            recipient[i] = "sip:" + recipients[i] + "@sipgate.net";
        }
        try {
            Object resp = client.execute("samurai.SessionInitiateMulti", new Object[] { "localhost", recipient, "text", message });
        } catch (XmlRpcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        String sipgateUsername = configService.getProperty("com.openexchange.sms.sipgate.username");
        String sipgatePassword = configService.getProperty("com.openexchange.sms.sipgate.password");
        try {
            config.setServerURL(new URL("https://api.sipgate.net/my/xmlrpcfacade/"));
        } catch (MalformedURLException e) {
            // will not happen
        }
        config.setBasicUserName(sipgateUsername);
        config.setBasicPassword(sipgatePassword);
        client.setConfig(config);

    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        // TODO Auto-generated method stub
        return null;
    }

}
