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

package com.openexchange.ajp13;

import java.io.IOException;
import com.openexchange.ajp13.exception.AJPv13BrokenCycleException;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13Exception.AJPCode;

/**
 * {@link AJPv13RequestBody} - Processes an incoming AJP request body.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13RequestBody extends AbstractAJPv13Request {

    @Override
    public void processRequest(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException {
        if (isPayloadNull()) {
            throw new AJPv13Exception(AJPCode.MISSING_PAYLOAD_DATA, true);
        }
        try {
            final int chunkContentLength;
            if (getPayloadLength() == 0 || (chunkContentLength = parseInt()) == 0) {
                /*
                 * Empty data package received
                 */
                if (ajpRequestHandler.isMoreDataExpected()) {
                    /*
                     * Hmm... we actually expect more data
                     */
                    if (LOG.isWarnEnabled()) {
                        final AJPv13Exception ajpExc = new AJPv13Exception(
                            AJPCode.UNEXPECTED_EMPTY_DATA_PACKAGE,
                            true,
                            Long.valueOf(ajpRequestHandler.getTotalRequestedContentLength()),
                            Long.valueOf(ajpRequestHandler.getContentLength()),
                            ajpRequestHandler.getForwardRequest());
                        ajpExc.fillInStackTrace();
                        LOG.warn(ajpExc.getMessage(), ajpExc);
                    }
                    /*
                     * Set data to null to indicate that no more data is available from web server
                     */
                    ajpRequestHandler.makeEqual();
                    ajpRequestHandler.setData(null);
                    return;
                }
                /*
                 * If we currently read from a chunked http input stream - transfer-encoding: chunked - , then 'content-length' header has not
                 * been set
                 */
                if (ajpRequestHandler.isNotSet() || ajpRequestHandler.isMoreDataReadThanExpected()) {
                    ajpRequestHandler.makeEqual();
                }
                ajpRequestHandler.setData(null);
                return;
            }
            /*
             * Parse current size
             */
            ajpRequestHandler.increaseTotalRequestedContentLength(chunkContentLength);
            final byte[] contentBytes = getByteSequence(chunkContentLength);
            /*
             * Add payload data to servlet's request input stream
             */
            ajpRequestHandler.setData(contentBytes);
        } catch (final IndexOutOfBoundsException e) {
            final AJPv13BrokenCycleException ajpExc = new AJPv13BrokenCycleException().setPayload(payloadData);
            /*
             * Dump package
             */
            final byte[] payload = payloadData;
            final byte[] clonedPackage = new byte[payload.length + 4];
            clonedPackage[0] = 0x12;
            clonedPackage[1] = 0x34;
            clonedPackage[2] = (byte) (dataLength >> 8);
            clonedPackage[3] = (byte) (dataLength & (255));
            System.arraycopy(payload, 0, clonedPackage, 4, payload.length);
            ajpExc.setDump(AJPv13Utility.dumpBytes(clonedPackage));
            throw ajpExc;
        }
    }

    /**
     * Initializes a new {@link AJPv13RequestBody}
     *
     * @param payloadData The body's payload data
     */
    public AJPv13RequestBody(final byte[] payloadData, final int dataLength) {
        super(payloadData, dataLength);
    }

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AJPv13RequestBody.class));

}
