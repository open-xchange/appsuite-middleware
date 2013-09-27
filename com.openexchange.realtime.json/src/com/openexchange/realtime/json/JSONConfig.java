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

package com.openexchange.realtime.json;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.server.Initialization;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;

/**
 * {@link JSONConfig} Collects and exposes configuration parameters needed by c.o.realtime.json
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class JSONConfig implements Initialization {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(JSONConfig.class);

    private final static String SESSION_WHITELIST_FILE = "noipcheck.cnf";

    private static final JSONConfig instance = new JSONConfig();

    public static JSONConfig getInstance() {
        return instance;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    // server.properties
    private boolean isIPCheckEnabled;

    private CookieHashSource cookieHashSource;

    private ClientWhitelist clientWhitelist;

    private SubnetMask allowedSubnet;

    private String ipMaskV4;

    private String ipMaskV6;

    private List<IPRange> ipRangeWhitelist;

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        init();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has no been started before");
            return;
        }
    }

    private void init() throws OXException {
        ConfigurationService configService = JSONServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }

        // server properties
        isIPCheckEnabled = configService.getBoolProperty("com.openexchange.IPCheck", true);
        cookieHashSource = CookieHashSource.parse(configService.getProperty("com.openexchange.cookie.hash", "calculate"));
        clientWhitelist = new ClientWhitelist().add(configService.getProperty("com.openexchange.IPCheckWhitelist", ""));
        ipMaskV4 = configService.getProperty("com.openexchange.IPMaskV4", "");
        ipMaskV6 = configService.getProperty("com.openexchange.IPMaskV6", "");
        allowedSubnet = new SubnetMask(ipMaskV4, ipMaskV6);
        ipRangeWhitelist = new LinkedList<IPRange>();
        initIpRangeWhitelist(configService);

    }

    /**
     * Init the IP ranges that should be excluded from the ip Checking during session validation.
     */
    private void initIpRangeWhitelist(ConfigurationService configService) {
        if (isIPCheckEnabled) {
            String text = configService.getText(SESSION_WHITELIST_FILE);
            if (text != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Exceptions from IP Check have been defined.");
                }
                final String[] lines = Strings.splitByCRLF(text);
                for (String line : lines) {
                    line = line.replaceAll("\\s", "");
                    if (!line.equals("") && (line.length() == 0 || line.charAt(0) != '#')) {
                        ipRangeWhitelist.add(IPRange.parseRange(line));
                    }
                }
            }
        }
    }

    /**
     * Gets the started
     * 
     * @return The started
     */
    public AtomicBoolean getStarted() {
        return started;
    }

    /**
     * Gets the isIPCheckEnabled
     * 
     * @return The isIPCheckEnabled
     */
    public boolean isIPCheckEnabled() {
        return isIPCheckEnabled;
    }

    /**
     * Gets the cookieHashSource
     * 
     * @return The cookieHashSource
     */
    public CookieHashSource getCookieHashSource() {
        return cookieHashSource;
    }

    /**
     * Gets the clientWhitelist, used to exclude single clients from IP checks.
     * 
     * @return The clientWhitelist, used to exclude single clients from IP checks
     */
    public ClientWhitelist getClientWhitelist() {
        return clientWhitelist;
    }

    /**
     * Gets the allowedSubnet
     * 
     * @return The allowedSubnet
     */
    public SubnetMask getAllowedSubnet() {
        return allowedSubnet;
    }

    /**
     * Gets the ipMaskV4
     * 
     * @return The ipMaskV4
     */
    public String getIpMaskV4() {
        return ipMaskV4;
    }

    /**
     * Gets the ipMaskV6
     * 
     * @return The ipMaskV6
     */
    public String getIpMaskV6() {
        return ipMaskV6;
    }

    
    /**
     * Gets the ipRangeWhitelist used to exclude clients from IP checks.
     *
     * @return The ipRangeWhitelist used to exclude clients from IP checks.
     */
    public List<IPRange> getIpRangeWhitelist() {
        return ipRangeWhitelist;
    }

}
