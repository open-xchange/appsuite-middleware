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

package com.openexchange.realtime.atmosphere;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.server.Initialization;
import com.openexchange.sessiond.impl.SubnetMask;

/**
 * {@link GrizzlyConfig} Collects and exposes configuration parameters needed by GrizzlOX
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereConfig implements Initialization {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmosphereConfig.class);

    private static final AtmosphereConfig instance = new AtmosphereConfig();

    public static AtmosphereConfig getInstance() {
        return instance;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    // server.properties
    private boolean isIPCheckEnabled;
    private CookieHashSource cookieHashSource;
    private ClientWhitelist clientWhitelist; 
    private SubnetMask allowedSubnet;
    private String ipv4Mask; 
    private String iPv6Mask;
    

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
        ConfigurationService configService = AtmosphereServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }

        // server properties
        this.isIPCheckEnabled = configService.getBoolProperty("com.openexchange.IPCheck", true);
        this.cookieHashSource = CookieHashSource.parse(configService.getProperty(" com.openexchange.cookie.hash", "calculate"));
        this.clientWhitelist = new ClientWhitelist().add(configService.getProperty("com.openexchange.IPCheckWhitelist", ""));
        final String ipMaskV4 = configService.getProperty("com.openexchange.IPMaskV4", "");
        final String ipMaskV6 = configService.getProperty("com.openexchange.IPMaskV6","");
        this.allowedSubnet = new SubnetMask(ipMaskV4, ipMaskV6);
        
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
     * Gets the clientWhitelist
     *
     * @return The clientWhitelist
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
     * Gets the ipv4Mask
     *
     * @return The ipv4Mask
     */
    public String getIpv4Mask() {
        return ipv4Mask;
    }

    
    /**
     * Gets the iPv6Mask
     *
     * @return The iPv6Mask
     */
    public String getiPv6Mask() {
        return iPv6Mask;
    }

}
