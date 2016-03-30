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

package com.openexchange.spamhandler.spamassassin.property;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.spamhandler.spamassassin.exceptions.SpamhandlerSpamassassinConfigurationExceptionCode;
import com.openexchange.spamhandler.spamassassin.osgi.Services;
import com.openexchange.spamhandler.spamassassin.osgi.SpamAssassinSpamHandlerActivator;

/**
 * A class which will deal with all property related actions.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class PropertyHandler {


    private interface IntegerSetterClosuse {

        void setter(final int value);

    }

    private enum Parameters {
        hostname("hostname"),
        port("port"),
        retries("retries"),
        retrysleep("retrysleep"),
        spamd("spamd"),
        timeout("timeout"),
        userSource("userSource");

        private final String name;

        private Parameters(final String name) {
            this.name = name;
        }

        public final String getName() {
            return bundlename + name;
        }
    }

    public static final String bundlename = "com.openexchange.spamhandler.spamassassin.";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyHandler.class);

    private static PropertyHandler singleton = new PropertyHandler();

    private String hostname;

    private final AtomicBoolean loaded = new AtomicBoolean();

    private int port;

    private int retries;

    private int retrysleep;

    private boolean spamd;

    private long timeout;

    public static PropertyHandler getInstance() {
        return singleton;
    }

    private static String checkStringProperty(final ConfigurationService conf, final Parameters param) throws OXException {
        final String name = param.getName();
        final String property = conf.getProperty(name);
        if (null == property) {
            throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NOT_SET.create(name);
        }
        return property;
    }

    private static String checkStringPropertyOptional(final ConfigurationService conf, final Parameters param) {
        final String name = param.getName();
        final String property = conf.getProperty(name, new PropertyListener() {

            @Override
            public void onPropertyChange(final PropertyEvent event) {
                switch (event.getType()) {
                case CHANGED:
                    // load all properties again, just to be sure...
                    LOG.info("Re-read configuration of Spamhandler Spamassassin bundle, because of property change");
                    try {
                        PropertyHandler.getInstance().loadProperties();
                    } catch (final OXException e) {
                        // As an exception at this point may only occur if something ugly has happened to the
                        // config file, the proper operation of this bundle is no more guaranteed. So it's
                        // safe to shutdown the bundle here...
                        SpamAssassinSpamHandlerActivator.shutdownBundle();
                    }
                    break;
                case DELETED:
                    // It might be possible that a delete in a config file makes sense so we just read in the
                    // whole configuration again
                    LOG.info("Re-read configuration of Spamhandler Spamassassin bundle, because of property deletion");
                    try {
                        PropertyHandler.getInstance().loadProperties();
                    } catch (final OXException e) {
                        // As an exception at this point may only occur if something ugly has happened to the
                        // config file, the proper operation of this bundle is no more guaranteed. So it's
                        // safe to shutdown the bundle here...
                        SpamAssassinSpamHandlerActivator.shutdownBundle();
                    }
                    break;
                default:
                    break;
                }
            }

        });
        if (null == property || 0 == property.length()) {
            return null;
        }
        return property;
    }

    public String getHostname() {
        return hostname;
    }


    public int getPort() {
        return port;
    }

    public int getRetries() {
        return retries;
    }

    public int getRetrysleep() {
        return retrysleep;
    }


    public long getTimeout() {
        return timeout;
    }


    public boolean isSpamd() {
        return spamd;
    }

    public void loadProperties() throws OXException {
        final StringBuilder logBuilder = new StringBuilder();

        final ConfigurationService configuration = Services.getService(ConfigurationService.class);

        logBuilder.append("\nLoading spamhandler spamassassin properties...\n");

        final String modestring = checkStringProperty(configuration, Parameters.spamd);
        try {
            this.setSpamd(Boolean.valueOf(modestring));
            logBuilder.append('\t').append(Parameters.spamd.getName()).append(": ").append(this.isSpamd()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw SpamhandlerSpamassassinConfigurationExceptionCode.MODE_TYPE_WRONG.create(modestring);
        }

        final String hostname = checkStringPropertyOptional(configuration, Parameters.hostname);
        if (null == hostname) {
            if (spamd) {
                throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NOT_SET_SPAMD.create(Parameters.hostname.getName());
            }
        } else {
            this.setHostname(hostname);
            logBuilder.append('\t').append(Parameters.hostname.getName()).append(": ").append(this.getHostname()).append('\n');
        }


        setIntegerParam(logBuilder, configuration, Parameters.port, new IntegerSetterClosuse() {
            @Override
            public void setter(final int value) {
                setPort(value);
            }
        });

        final String timeoutstring = checkStringPropertyOptional(configuration, Parameters.timeout);
        if (null == timeoutstring) {
            if (spamd) {
                throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NOT_SET_SPAMD.create(Parameters.timeout.getName());
            }
        } else {
            try {
                this.setTimeout(Long.parseLong(timeoutstring));
                logBuilder.append('\t').append(Parameters.timeout.getName()).append(": ").append(this.getTimeout()).append('\n');
            } catch (final NumberFormatException e) {
                throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NO_LONG.create(Parameters.timeout.getName(), timeoutstring);
            }
        }

        setIntegerParam(logBuilder, configuration, Parameters.retries, new IntegerSetterClosuse() {
            @Override
            public void setter(final int value) {
                setRetries(value);
            }
        });

        setIntegerParam(logBuilder, configuration, Parameters.retrysleep, new IntegerSetterClosuse() {
            @Override
            public void setter(final int value) {
                setRetrysleep(value);
            }
        });

        this.loaded.set(true);
        LOG.info(logBuilder.toString());
    }

    public void reloadProperties() {

    }


    private void setHostname(final String hostname) {
        this.hostname = hostname;
    }


    private void setIntegerParam(final StringBuilder logBuilder, final ConfigurationService configuration, final Parameters param, final IntegerSetterClosuse closure) throws OXException {
        final String value = checkStringPropertyOptional(configuration, param);
        if (null == value) {
            if (spamd) {
                throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NOT_SET_SPAMD.create(param.getName());
            }
        } else {
            try {
                final int parseInt = Integer.parseInt(value);
                closure.setter(parseInt);
                logBuilder.append('\t').append(param.getName()).append(": ").append(parseInt).append('\n');
            } catch (final NumberFormatException e) {
                throw SpamhandlerSpamassassinConfigurationExceptionCode.PARAMETER_NO_INTEGER.create(param.getName(), value);
            }
        }
    }

    private void setPort(final int port) {
        this.port = port;
    }

    private void setRetries(final int retries) {
        this.retries = retries;
    }

    private void setRetrysleep(final int retrysleep) {
        this.retrysleep = retrysleep;
    }

    private void setSpamd(final boolean mode) {
        this.spamd = mode;
    }

    private void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

}
