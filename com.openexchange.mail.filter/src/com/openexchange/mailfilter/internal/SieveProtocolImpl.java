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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailfilter.internal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.export.SieveHandler;
import com.openexchange.jsieve.export.SieveHandler.MetricHelper;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerException;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.NoResponseHandler;
import com.openexchange.mailfilter.SieveProtocol;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;

/**
 * {@link SieveProtocolImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class SieveProtocolImpl implements SieveProtocol {

    /**
     * The constant for CRLF (carriage-return line-feed).
     */
    private final static String CRLF = "\r\n";

    /**
     * The SIEVE OK.
     */
    private final static String SIEVE_OK = "OK";

    /**
     * The SIEVE NO.
     */
    private final static String SIEVE_NO = "NO";

    /**
     * Special NO response signaling that authentication failed.
     */
    private final static String SIEVE_AUTH_FAILED = "NO \"Authentication Error\"";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final SieveHandler sieveHandler;
    private MetricHelper metricArgs;
    private final Credentials credentials;
    private final boolean useSIEVEResponseCodes;

    /**
     * Initializes a new {@link SieveProtocolImpl}.
     *
     * @param sieveHandler The SIEVE handler
     * @param credentials The credentials used to initialize SIEVE handler
     * @param useSIEVEResponseCodes Whether to use SIEVE response codes
     */
    public SieveProtocolImpl(SieveHandler sieveHandler, Credentials credentials, boolean useSIEVEResponseCodes) {
        super();
        this.sieveHandler = sieveHandler;
        this.metricArgs = null;
        this.credentials = credentials;
        this.useSIEVEResponseCodes = useSIEVEResponseCodes;
    }

    @Override
    public void write(String... commandLines) throws OXException {
        try {
            if (metricArgs == null) {
                metricArgs = sieveHandler.createMetricHelper();
            }

            BufferedOutputStream bos_sieve = sieveHandler.getOutput();
            for (String commandLine : commandLines) {
                if (Strings.isNotEmpty(commandLine)) {
                    if (!commandLine.endsWith(CRLF)) {
                        commandLine = new StringBuilder(commandLine).append(CRLF).toString();
                    }
                    bos_sieve.write(commandLine.getBytes(StandardCharsets.UTF_8));
                }
            }
            bos_sieve.flush();
        } catch (IOException e) {
            throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
        }
    }

    @Override
    public String[] readResponseLines() throws OXException {
        try {
            List<String> list = new ArrayList<String>();
            while (true) {
                String temp = sieveHandler.readResponseLine(metricArgs);
                if (null == temp) {
                    throw new OXSieveHandlerException("Communication to SIEVE server aborted. ", sieveHandler.getSieveHost(), sieveHandler.getSievePort(), null);
                }
                list.add(temp);
                if (temp.startsWith(SIEVE_OK) || temp.startsWith(SIEVE_NO)) {
                    break;
                }
            }
            return list.toArray(new String[list.size()]);
        } catch (OXSieveHandlerException e) {
            throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
        } catch (IOException e) {
            throw MailFilterExceptionCode.IO_CONNECTION_ERROR.create(e, sieveHandler.getSieveHost(), Integer.valueOf(sieveHandler.getSievePort()));
        } finally {
            metricArgs = null;
        }
    }

    @Override
    public void handleResponse(String responseLine, Optional<NoResponseHandler> optionalHandler) throws OXException {
        if (responseLine != null) {
            if (responseLine.startsWith(SIEVE_OK)) {
                return;
            }
            try {
                if (responseLine.startsWith(SIEVE_AUTH_FAILED)) {
                    throw new OXSieveHandlerException("can't auth to SIEVE ", sieveHandler.getSieveHost(), sieveHandler.getSievePort(), sieveHandler.parseSIEVEResponse(responseLine, null));
                }
                if (responseLine.startsWith(SIEVE_NO)) {
                    String errorMessage = optionalHandler.isPresent() ? optionalHandler.get().getErrorMessageFor(responseLine) : "Command failed";
                    throw new OXSieveHandlerException(errorMessage, sieveHandler.getSieveHost(), sieveHandler.getSievePort(), sieveHandler.parseSIEVEResponse(responseLine, null));
                }
            } catch (OXSieveHandlerException e) {
                throw MailFilterExceptionCode.handleParsingException(e, credentials, useSIEVEResponseCodes);
            }
        }
    }

}
