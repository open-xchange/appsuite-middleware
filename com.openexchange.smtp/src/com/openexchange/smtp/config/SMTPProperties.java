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

package com.openexchange.smtp.config;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.smtp.services.Services;

/**
 * {@link SMTPProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPProperties extends AbstractProtocolProperties implements ISMTPProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SMTPProperties.class);

    private static final SMTPProperties instance = new SMTPProperties();

    /**
     * Gets the singleton instance of {@link SMTPProperties}
     *
     * @return The singleton instance of {@link SMTPProperties}
     */
    public static SMTPProperties getInstance() {
        return instance;
    }

    /*
     * Fields for global properties
     */

    private final ITransportProperties transportProperties;

    private String smtpLocalhost;

    private boolean smtpAuth;

    private boolean smtpEnvelopeFrom;

    private boolean sendPartial;

    private String smtpAuthEnc;

    private int smtpTimeout;

    private int smtpConnectionTimeout;

    private boolean logTransport;

    private String sslProtocols;

    private String cipherSuites;

    private String primaryAdressHeader;

    /**
     * Initializes a new {@link SMTPProperties}
     */
    private SMTPProperties() {
        super();
        transportProperties = TransportProperties.getInstance();
    }

    @Override
    protected void loadProperties0() throws OXException {
        StringBuilder logBuilder = new StringBuilder(1024);
        List<Object> args = new ArrayList<Object>(32);
        String lineSeparator = Strings.getLineSeparator();

        logBuilder.append("{}Loading global SMTP properties...{}");
        args.add(lineSeparator);
        args.add(lineSeparator);

        final ConfigurationService configuration = Services.getService(ConfigurationService.class);
        {
            final String smtpLocalhostStr = configuration.getProperty("com.openexchange.smtp.smtpLocalhost").trim();
            smtpLocalhost = (smtpLocalhostStr == null) || (smtpLocalhostStr.length() == 0) || "null".equalsIgnoreCase(smtpLocalhostStr) ? null : smtpLocalhostStr;
            logBuilder.append("    SMTP Localhost: {}{}");
            args.add(smtpLocalhost);
            args.add(lineSeparator);
        }

        {
            final String smtpAuthStr = configuration.getProperty("com.openexchange.smtp.smtpAuthentication", "false").trim();
            smtpAuth = Boolean.parseBoolean(smtpAuthStr);
            logBuilder.append("    SMTP Authentication: {}{}");
            args.add(smtpAuth);
            args.add(lineSeparator);
        }

        {
            final String sendPartialStr = configuration.getProperty("com.openexchange.smtp.sendPartial", "false").trim();
            sendPartial = Boolean.parseBoolean(sendPartialStr);
            logBuilder.append("    Send Partial: {}{}");
            args.add(sendPartial);
            args.add(lineSeparator);
        }

        {
            final String smtpEnvFromStr = configuration.getProperty("com.openexchange.smtp.setSMTPEnvelopeFrom", "false").trim();
            smtpEnvelopeFrom = Boolean.parseBoolean(smtpEnvFromStr);
            logBuilder.append("    Set SMTP ENVELOPE-FROM: {}{}");
            args.add(smtpEnvelopeFrom);
            args.add(lineSeparator);
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.smtp.logTransport", "false").trim();
            logTransport = Boolean.parseBoolean(tmp);
            logBuilder.append("    Log transport: {}{}");
            args.add(logTransport);
            args.add(lineSeparator);
        }

        {
            final String smtpAuthEncStr = configuration.getProperty("com.openexchange.smtp.smtpAuthEnc", "UTF-8").trim();
            if (CharsetDetector.isValid(smtpAuthEncStr)) {
                smtpAuthEnc = smtpAuthEncStr;
                logBuilder.append("    SMTP Auth Encoding: ").append(smtpAuthEnc).append('\n');
                args.add(smtpAuthEnc);
                args.add(lineSeparator);
            } else {
                smtpAuthEnc = "UTF-8";
                logBuilder.append("    SMTP Auth Encoding: Unsupported charset \"{}\". Setting to fallback {}{}");
                args.add(smtpAuthEncStr);
                args.add(smtpAuthEnc);
                args.add(lineSeparator);
            }
        }

        {
            final String smtpTimeoutStr = configuration.getProperty("com.openexchange.smtp.smtpTimeout", "5000").trim();
            try {
                smtpTimeout = Integer.parseInt(smtpTimeoutStr);
                logBuilder.append("    SMTP Timeout: {}{}");
                args.add(smtpTimeout);
                args.add(lineSeparator);
            } catch (final NumberFormatException e) {
                smtpTimeout = 5000;
                logBuilder.append("    SMTP Timeout: Invalid value \"{}\". Setting to fallback {}{}");
                args.add(smtpTimeoutStr);
                args.add(smtpTimeout);
                args.add(lineSeparator);

            }
        }

        {
            final String smtpConTimeoutStr = configuration.getProperty("com.openexchange.smtp.smtpConnectionTimeout", "10000").trim();
            try {
                smtpConnectionTimeout = Integer.parseInt(smtpConTimeoutStr);
                logBuilder.append("    SMTP Connection Timeout: {}{}");
                args.add(smtpConnectionTimeout);
                args.add(lineSeparator);
            } catch (final NumberFormatException e) {
                smtpConnectionTimeout = 10000;
                logBuilder.append("    SMTP Connection Timeout: Invalid value \"{}\". Setting to fallback {}{}");
                args.add(smtpConTimeoutStr);
                args.add(smtpConnectionTimeout);
                args.add(lineSeparator);

            }
        }

        sslProtocols = configuration.getProperty("com.openexchange.smtp.ssl.protocols", "SSLv3 TLSv1").trim();
        logBuilder.append("    Supported SSL protocols: {}{}");
        args.add(sslProtocols);
        args.add(lineSeparator);

        {
            final String tmp = configuration.getProperty("com.openexchange.smtp.ssl.ciphersuites", "").trim();
            this.cipherSuites = Strings.isEmpty(tmp) ? null : tmp;
            logBuilder.append("    Supported SSL cipher suites: {}{}");
            args.add(null == this.cipherSuites ? "<default>" : cipherSuites);
            args.add(lineSeparator);
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.smtp.setPrimaryAddressHeader", "").trim();
            primaryAdressHeader = (tmp == null) || (tmp.length() == 0) || "null".equalsIgnoreCase(tmp) ? null : tmp;
            logBuilder.append("    Primary address header: {}{}");
            args.add(primaryAdressHeader);
            args.add(lineSeparator);
        }

        logBuilder.append("Global SMTP properties successfully loaded!");
        LOG.info(logBuilder.toString(), args.toArray(new Object[args.size()]));
    }

    @Override
    protected void resetFields() {
        smtpLocalhost = null;
        sendPartial = false;
        smtpAuth = false;
        smtpEnvelopeFrom = false;
        smtpAuthEnc = null;
        smtpTimeout = 0;
        smtpConnectionTimeout = 0;
        logTransport = false;
        sslProtocols = "SSLv3 TLSv1";
        cipherSuites = null;
        primaryAdressHeader = null;
    }

    @Override
    public String getSmtpLocalhost() {
        return smtpLocalhost;
    }

    @Override
    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    @Override
    public boolean isSendPartial() {
        return sendPartial;
    }

    @Override
    public boolean isSmtpEnvelopeFrom() {
        return smtpEnvelopeFrom;
    }

    @Override
    public boolean isLogTransport() {
        return logTransport;
    }

    @Override
    public String getSmtpAuthEnc() {
        return smtpAuthEnc;
    }

    @Override
    public int getSmtpTimeout() {
        return smtpTimeout;
    }

    @Override
    public int getSmtpConnectionTimeout() {
        return smtpConnectionTimeout;
    }

    @Override
    public int getReferencedPartLimit() {
        return transportProperties.getReferencedPartLimit();
    }

    @Override
    public String getSSLProtocols() {
        return sslProtocols;
    }

    @Override
    public String getSSLCipherSuites() {
        return cipherSuites;
    }

    @Override
    public String getPrimaryAddressHeader() {
        return primaryAdressHeader;
    }
}
