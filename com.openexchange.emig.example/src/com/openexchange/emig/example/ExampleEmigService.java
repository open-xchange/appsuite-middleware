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

package com.openexchange.emig.example;

import com.openexchange.config.ConfigurationService;
import com.openexchange.emig.EmigService;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ExampleEmigService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ExampleEmigService implements EmigService {
    org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExampleEmigService.class);
    
    // Access to system services. Be sure the services are also
    // declared as dependencies in #getNeededServices
    // of the activator.
    private ServiceLookup services;

    public ExampleEmigService(ServiceLookup services) {
        super();
        this.services = services;
    }
    
    @Override
    public boolean isEMIG_Session(String userIdentifier) throws OXException {
        // We will assume that every user has access to the EMIG system. Note that access to emig 
        // can also be controlled by setting the property "com.openexchange.emig.enabled" to true or false in
        // the config cascade
        return true;
    }

    @Override
    public boolean isEMIG_MSA(String serverName, String mailFrom, String debugLoginname) throws OXException {
        // This method is supposed to check, whether the SMTP transport server can establish a secured connection for the 'mailFrom' address
        // This example will implement this in the following manner: We will contact a webserver at a configured base URL and construct a URL
        // 'http://[configuredHost]/[configuredPath]/smtpServers/[serverName]?from=[mailFrom]'
        // and check the return code. If it is 200 (OK), then we accept the server as secure. So the trivial implementation of the 
        // backend is an apache server with a file for each known SMTP server. 
        LOG.debug("isEMIG_MSA(\"" + serverName + "\", \"" + mailFrom + "\", ","\"" + debugLoginname + "\")");
        StringBuilder b = new StringBuilder(getPrefix());
        b.append("smtpServers/").append(serverName);
        LOG.debug("Trying " + b);
        
        HTTPResponse response = services.getService(HTTPClient.class).getBuilder().get().url(b.toString()).parameter("from", mailFrom).build().execute();
        LOG.debug("Status: " + response.getStatus());
        return response.getStatus() == 200;
    }

    @Override
    public int[] isEMIG_Recipient(String[] mailAddresses) throws OXException {
        // This method is supposed to check whether a mail address is part of the emig network (grey == MEMBER == 1) or
        // can be reached securely (green == SECURE == 2) or we know nothing about this address (no marking == NONE == 0)
        // This is again implemented by consulting a webserver. First we'll try to find out if an address is considered secure:
        // 'http://[configuredHost]/[configuredPath]/secureAddresses/[mailAddress]'
        // If this call gives a 200 (OK) status, we consider the mail address secure. Failing that we check
        // 'http://[configuredHost]/[configuredPath]/memberAddresses/[mailAddress]'
        // If this call gives a 200 (OK) status we consider the mail address a member, otherwise we consider it unknown
        LOG.info("isEMIG_Recipient("+pp(mailAddresses)+")");
        int[] response = new int[mailAddresses.length];
        for(int i = 0; i < response.length; i++) {
            response[i] = check(mailAddresses[i]);
        }
        for (int i = 0; i < response.length; i++) {
            LOG.debug(mailAddresses[i] + ": " + response[i]);
        }
        return response;
    }
    private int check(String mailAddress) throws OXException {
        StringBuilder b = new StringBuilder(getPrefix()).append("secureAddresses/").append(mailAddress);
        LOG.debug("Trying " + b);
        HTTPResponse response = services.getService(HTTPClient.class).getBuilder().get().url(b.toString()).build().execute();
        LOG.debug("Status: " + response.getStatus());
        if (response.getStatus() == 200) {
            return SECURE;
        }
        
        b = new StringBuilder(getPrefix()).append("memberAddresses/").append(mailAddress);
        LOG.debug("Trying " + b);
        response = services.getService(HTTPClient.class).getBuilder().get().url(b.toString()).build().execute();
        LOG.debug("Status: " + response.getStatus());
        if (response.getStatus() == 200) {
            return MEMBER;
        }
        
        return NONE;
    }

    private String pp(String[] mailAddresses) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (String string : mailAddresses) {
            b.append('"').append(string).append("\", ");
        }
        b.setLength(b.length()-3);
        b.append(']');
        return b.toString();
    }
    
    private String getPrefix() {
        String property = services.getService(ConfigurationService.class).getProperty("com.openexchange.emig.example.prefix");
        if (property == null) {
            return "http://localhost/emig/";
        }
        if (property.endsWith("/")) {
            return property;
        }
        return property + "/";
    }

}
