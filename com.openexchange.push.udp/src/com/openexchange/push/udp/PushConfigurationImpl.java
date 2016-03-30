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

package com.openexchange.push.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * PushConfigInterface
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class PushConfigurationImpl extends AbstractConfigWrapper implements PushConfiguration {

    private boolean isPushEnabled = false;

    private Set<RemoteHostObject> remoteHost = new HashSet<RemoteHostObject>();

    private int registerTimeout = 3600000;

    private int outputQueueDelay = 120000;

    private int registerPort = 44335;

    private boolean isRegisterDistributionEnabled = false;

    private boolean isEventDistributionEnabled = false;

    private InetAddress senderAddress;

    private boolean multicastEnabled = false;

    private InetAddress multicastAddress;

    private InetAddress hostname;

    private int multicastPort = 0;

    private int remoteHostTimeOut = 3600000;

    private int remoteHostRefresh = 30000;

    private boolean isInit = false;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushConfigurationImpl.class);

    public PushConfigurationImpl(final ConfigurationService conf) throws OXException {
        this(conf, false);
    }

    public PushConfigurationImpl(final ConfigurationService conf, final boolean ignoreIsInit) throws OXException {
        if (!ignoreIsInit && isInit) {
            return;
        }

        isPushEnabled = parseProperty(conf, "com.openexchange.push.udp.pushEnabled", isPushEnabled);
        LOG.debug("PushHandler property: com.openexchange.push.udp.pushEnabled={}", isPushEnabled);

        registerPort = parseProperty(conf, "com.openexchange.push.udp.registerPort", registerPort);
        LOG.debug("PushHandler property: com.openexchange.push.udp.registerPort={}", registerPort);

        String[] remoteAddressAndPort = null;
        remoteAddressAndPort = parseProperty(conf, "com.openexchange.push.udp.remoteHost", remoteAddressAndPort);
        LOG.debug("PushHandler property: com.openexchange.push.udp.remoteHost={}", Arrays.toString(remoteAddressAndPort));

        if (remoteAddressAndPort != null) {
            for (final String element : remoteAddressAndPort) {
                final RemoteHostObject remoteHostObject = new RemoteHostObject();
                final String[] addressAndPort = element.split(":");
                try {
                    if (addressAndPort.length == 1) {
                        remoteHostObject.setHost(InetAddress.getByName(addressAndPort[0]));
                        remoteHostObject.setPort(registerPort);
                    } else if (addressAndPort.length >= 2) {
                        remoteHostObject.setHost(InetAddress.getByName(addressAndPort[0]));
                        remoteHostObject.setPort(Integer.parseInt(addressAndPort[1]));
                    }
                } catch (final UnknownHostException exc) {
                    LOG.error("problem with parsing remote host attribute: {}", addressAndPort[0], exc);
                }

                remoteHost.add(remoteHostObject);
            }
        }

        registerTimeout = parseProperty(conf, "com.openexchange.push.udp.registerTimeout", registerTimeout);
        LOG.debug("PushHandler property: com.openexchange.push.udp.registerTimeout={}", registerTimeout);

        outputQueueDelay = parseProperty(conf, "com.openexchange.push.udp.outputQueueDelay", outputQueueDelay);
        LOG.debug("PushHandler property: com.openexchange.push.udp.outputQueueDelay={}", outputQueueDelay);

        isRegisterDistributionEnabled = parseProperty(
            conf,
            "com.openexchange.push.udp.registerDistributionEnabled",
            isRegisterDistributionEnabled);
        LOG.debug("PushHandler property: com.openexchange.push.udp.registerDistributionEnabled={}", isRegisterDistributionEnabled);

        isEventDistributionEnabled = parseProperty(conf, "com.openexchange.push.udp.eventDistributionEnabled", isEventDistributionEnabled);
        LOG.debug("PushHandler property: com.openexchange.push.udp.eventDistributionEnabled={}", isEventDistributionEnabled);

        String senderAddressString = null;
        senderAddressString = parseProperty(conf, "com.openexchange.push.udp.senderAddress", senderAddressString);
        LOG.debug("PushHandler property: com.openexchange.push.udp.senderAddress={}", senderAddressString);

        try {
            if (senderAddressString != null) {
                senderAddress = InetAddress.getByName(senderAddressString);
            }
        } catch (final UnknownHostException exc) {
            LOG.error("problem with parsing sender address: {}", senderAddressString, exc);
        }

        remoteHostTimeOut = parseProperty(conf, "com.openexchange.push.udp.remoteHostTimeOut", remoteHostTimeOut);
        LOG.debug("PushHandler property: com.openexchange.push.udp.remoteHostTimeOut={}", remoteHostTimeOut);

        remoteHostRefresh = parseProperty(conf, "com.openexchange.push.udp.remoteHostRefresh", remoteHostRefresh);
        LOG.debug("PushHandler property: com.openexchange.push.udp.remoteHostRefresh={}", remoteHostRefresh);

        multicastEnabled = parseProperty(conf, "com.openexchange.push.udp.multicastEnabled", multicastEnabled);
        LOG.debug("PushHandler property: com.openexchange.push.udp.multicastEnabled={}", multicastEnabled);

        String multicastAddressString = null;
        multicastAddressString = parseProperty(conf, "com.openexchange.push.udp.multicastAddress", multicastAddressString);
        LOG.debug("PushHandler property: com.openexchange.push.udp.multicastAddress={}", multicastAddressString);

        try {
            multicastAddress = InetAddress.getByName(multicastAddressString);
        } catch (final UnknownHostException exc) {
            LOG.error("problem with parsing multicast address: {}", multicastAddressString, exc);
        }

        multicastPort = parseProperty(conf, "com.openexchange.push.udp.multicastPort", multicastPort);
        LOG.debug("PushHandler property: com.openexchange.push.udp.multicastPort={}", multicastPort);

        String hostnameString = parseProperty(conf, "com.openexchange.push.udp.hostname", (String) null);
        LOG.debug("PushHandler property: com.openexchange.push.udp.hostname={}", hostnameString);
        try {
            if (null != hostnameString) {
                hostname = InetAddress.getByName(hostnameString);
            } else {
                hostname = InetAddress.getLocalHost();
            }
        } catch (UnknownHostException e) {
            LOG.error("Unable to determine internet address for hostname: {}", hostnameString, e);
        }
        
        if (hostname == null) {
            throw PushUDPExceptionCode.UNRESOLVABLE_HOSTNAME.create(hostnameString);
        }
        
        LOG.info("Using {} for inter OX UDP communication.", hostname.getHostAddress());

        isInit = true;
    }

    @Override
    public boolean isPushEnabled() {
        return isPushEnabled;
    }

    @Override
    public void setPushEnabled(final boolean isPushEnabled) {
        this.isPushEnabled = isPushEnabled;
    }

    @Override
    public Set<RemoteHostObject> getRemoteHost() {
        return remoteHost;
    }

    @Override
    public void setRemoteHost(final Set<RemoteHostObject> remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public int getRegisterTimeout() {
        return registerTimeout;
    }

    @Override
    public void setRegisterTimeout(final int registerTimeout) {
        this.registerTimeout = registerTimeout;
    }

    @Override
    public int getRegisterPort() {
        return registerPort;
    }

    @Override
    public void setRegisterPort(final int registerPort) {
        this.registerPort = registerPort;
    }

    @Override
    public boolean isRegisterDistributionEnabled() {
        return isRegisterDistributionEnabled;
    }

    @Override
    public void setRegisterDistributionEnabled(final boolean isRegisterDistributionEnabled) {
        this.isRegisterDistributionEnabled = isRegisterDistributionEnabled;
    }

    @Override
    public boolean isEventDistributionEnabled() {
        return isEventDistributionEnabled;
    }

    @Override
    public void setEventDistributionEnabled(final boolean isEventDistributionEnabled) {
        this.isEventDistributionEnabled = isEventDistributionEnabled;
    }

    @Override
    public int getOutputQueueDelay() {
        return outputQueueDelay;
    }

    @Override
    public void setOutputQueueDelay(final int outputQueueDelay) {
        this.outputQueueDelay = outputQueueDelay;
    }

    @Override
    public InetAddress getSenderAddress() {
        return senderAddress;
    }

    @Override
    public void setSenderAddress(final InetAddress senderAddress) {
        this.senderAddress = senderAddress;
    }

    @Override
    public boolean isMultiCastEnabled() {
        return multicastEnabled;
    }

    @Override
    public void setMultiCastEnabled(final boolean multicastEnabled) {
        this.multicastEnabled = multicastEnabled;
    }

    @Override
    public int getMultiCastPort() {
        return multicastPort;
    }

    @Override
    public InetAddress getMultiCastAddress() {
        return multicastAddress;
    }

    @Override
    public void setMultiCastAddress(final InetAddress multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    @Override
    public int getRemoteHostTimeOut() {
        return remoteHostTimeOut;
    }

    @Override
    public void setRemoteHostTimeOut(final int remoteHostTimeOut) {
        this.remoteHostTimeOut = remoteHostTimeOut;
    }

    @Override
    public int getRemoteHostRefresh() {
        return remoteHostRefresh;
    }

    @Override
    public void setRemoteHostRefresh(final int remoteHostRefresh) {
        this.remoteHostRefresh = remoteHostRefresh;
    }

    @Override
    public InetAddress getHostName() {
        return hostname;
    }
}
