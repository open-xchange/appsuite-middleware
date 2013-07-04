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

package com.openexchange.ajp13.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import com.openexchange.ajp13.AJPv13ServletOutputStream;
import com.openexchange.ajp13.Services;
import com.openexchange.ajp13.util.CharsetValidator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.tools.regex.MatcherReplacer;

/**
 * {@link ServletResponseWrapper} - Wrapper for {@link ServletResponse}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServletResponseWrapper implements ServletResponse {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ServletResponseWrapper.class));

    public static final int OUTPUT_NOT_SELECTED = -1;

    public static final int OUTPUT_STREAM = 1;

    public static final int OUTPUT_WRITER = 2;

    private static volatile String defaultCharset;

    private static String getDefaultCharset() {
        String tmp = defaultCharset;
        if (tmp == null) {
            synchronized (ServletResponseWrapper.class) {
                tmp = defaultCharset;
                if (tmp == null) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return "UTF-8";
                    }
                    defaultCharset = tmp = service.getProperty("DefaultEncoding", "UTF-8");
                }
            }
        }
        return tmp;
    }

    protected static final String CONTENT_TYPE = "Content-Type";

    protected static final String CONTENT_LENGTH = "Content-Length";

    protected String characterEncoding;

    protected int status;

    protected final Map<String, String[]> headers;

    protected Locale locale;

    protected boolean committed;

    protected int bufferSize;

    protected int bytePosition;

    protected AJPv13ServletOutputStream servletOutputStream;

    protected PrintWriter writer;

    protected int outputSelection;

    /**
     * Initializes a new {@link ServletResponseWrapper}
     */
    public ServletResponseWrapper() {
        super();
        headers = new HashMap<String, String[]>(16);
        outputSelection = OUTPUT_NOT_SELECTED;
    }

    private static final Pattern CONTENT_TYPE_CHARSET_PARAM = Pattern.compile("(;\\s*charset=)([^\\s|^;]+)");

    @Override
    public void setContentType(final String contentType) {
        if (contentType == null) {
            return;
        }
        final Matcher m = CONTENT_TYPE_CHARSET_PARAM.matcher(contentType);
        if (m.find()) {
            /*
             * Check if getWriter() was already called
             */
            if (outputSelection == OUTPUT_WRITER && !characterEncoding.equalsIgnoreCase(m.group(2))) {
                throw new IllegalStateException(
                    "\"getWriter()\" has already been called. " + "Not allowed to change its encoding afterwards");
            }
            do {
                setCharacterEncoding(m.group(2));
            } while (m.find());
        } else if (characterEncoding == null) {
            /*
             * Corresponding to rfc
             */
            setCharacterEncoding(getDefaultCharset());
        }
        headers.put(CONTENT_TYPE, new String[] { contentType });
    }

    @Override
    public String getContentType() {
        return (headers.get(CONTENT_TYPE))[0];
    }

    @Override
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setContentLength(final int contentLength) {
        headers.put(CONTENT_LENGTH, new String[] { Integer.toString(contentLength) });
    }

    public int getContentLength() {
        return headers.containsKey(CONTENT_LENGTH) ? Integer.parseInt((headers.get(CONTENT_LENGTH))[0]) : 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (outputSelection == OUTPUT_WRITER && writer != null) {
            // Flush through print writer
            writer.flush();
        } else if (outputSelection == OUTPUT_STREAM && servletOutputStream != null) {
            // Flush directly on servlet output stream
            servletOutputStream.flush();
        }
    }

    /**
     * Sets the character encoding
     *
     * @param characterEncoding
     */
    @Override
    public void setCharacterEncoding(final String characterEncoding) {
        /*
         * Check if getWriter() was already called
         */
        if (outputSelection == OUTPUT_WRITER && !characterEncoding.equalsIgnoreCase(characterEncoding)) {
            throw new IllegalStateException("\"getWriter()\" has already been called. " + "Not allowed to change its encoding afterwards");
        }
        setCharacterEncoding(characterEncoding, true);
    }

    /**
     * Sets the character encoding
     *
     * @param characterEncoding
     * @param checkContentType
     */
    private void setCharacterEncoding(final String characterEncoding, final boolean checkContentType) {
        this.characterEncoding = characterEncoding;
        if (checkContentType && headers.containsKey(CONTENT_TYPE)) {
            final String contentType = (headers.get(CONTENT_TYPE))[0];
            final Matcher m = CONTENT_TYPE_CHARSET_PARAM.matcher(contentType);
            if (m.find()) {
                /*
                 * Charset argument set in content type and differs from new charset
                 */
                if (!characterEncoding.equalsIgnoreCase(m.group(2))) {
                    final StringBuilder newContentType = new StringBuilder();
                    final MatcherReplacer mr = new MatcherReplacer(m, contentType);
                    mr.appendLiteralReplacement(newContentType, new com.openexchange.java.StringAllocator().append(m.group(1)).append(characterEncoding).toString());
                    while (m.find()) {
                        mr.appendLiteralReplacement(
                            newContentType,
                            new com.openexchange.java.StringAllocator().append(m.group(1)).append(characterEncoding).toString());
                    }
                    mr.appendTail(newContentType);
                    headers.put(CONTENT_TYPE, new String[] { newContentType.toString() });
                }
            } else {
                /*
                 * No charset argument set in content type, yet
                 */
                final String newCT = contentType + "; charset=" + characterEncoding;
                headers.put(CONTENT_TYPE, new String[] { newCT });
            }
        }
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding == null ? (characterEncoding = getDefaultCharset()) : characterEncoding;
    }

    @Override
    public void resetBuffer() {
        if (committed) {
            throw new IllegalStateException("resetBuffer(): The response has already been committed");
        }
        if (outputSelection == OUTPUT_WRITER && writer != null) {
            try {
                if (bufferSize > 0) {
                    writer =
                        new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(servletOutputStream, getCharacterEncoding()), bufferSize),
                            true);
                } else {
                    writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(servletOutputStream, getCharacterEncoding())), true);
                }
            } catch (final UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
            }
        } else if (outputSelection == OUTPUT_STREAM && servletOutputStream != null) {
            servletOutputStream.resetBuffer();
        }
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException, IOException {
        if (writer != null) {
            return writer;
        }
        if (servletOutputStream == null) {
            throw new IOException("no ServletOutputStream found!");
        }
        if (characterEncoding == null) {
            /*
             * Method setContentType() has not been called prior to call getWriter()
             */
            characterEncoding = getDefaultCharset();
        }
        /*
         * Check Charset Encoding
         */
        CharsetValidator.getInstance().checkCharset(characterEncoding);
        /*
         * Check if getOutputSteam hasn't been called before
         */
        if (outputSelection == OUTPUT_STREAM) {
            throw new IllegalStateException("Servlet's OutputStream has already been selected as output");
        }
        if (outputSelection == OUTPUT_NOT_SELECTED) {
            outputSelection = OUTPUT_WRITER;
        }
        if (bufferSize > 0) {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(servletOutputStream, characterEncoding), bufferSize), true);
        } else {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(servletOutputStream, characterEncoding)), true);
        }
        return writer;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Sets the committed flag
     *
     * @param committed
     */
    public void setCommitted(final boolean committed) {
        this.committed = committed;
    }

    @Override
    public void setBufferSize(final int bufferSize) {
        if (outputSelection != OUTPUT_NOT_SELECTED) {
            throw new IllegalStateException("Buffer size MUSTN'T be altered when body content has already been written/selected.");
        }
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the underlying {@link AJPv13ServletOutputStream} reference
     *
     * @param os
     */
    public void setServletOutputStream(final AJPv13ServletOutputStream os) {
        servletOutputStream = os;
    }

    /**
     * @return the underlying {@link AJPv13ServletOutputStream} reference
     */
    public AJPv13ServletOutputStream getServletOutputStream() {
        return servletOutputStream;
    }

    /**
     * Removes the underlying {@link AJPv13ServletOutputStream} reference by setting it to <code>null</code>
     */
    public void removeServletOutputStream() {
        servletOutputStream = null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            throw new IOException("no ServletOutputStream found!");
        }
        /*
         * Check if getOutputSteam hasn't been called before
         */
        if (outputSelection == OUTPUT_WRITER) {
            throw new IllegalStateException("Servlet's Writer has already been selected as output");
        }
        if (outputSelection == OUTPUT_NOT_SELECTED) {
            outputSelection = OUTPUT_STREAM;
        }
        return servletOutputStream;
    }

    @Override
    public void reset() {
        if (committed) {
            throw new IllegalStateException("Servlet can not be resetted cause it has already been committed");
        }
        headers.clear();
        status = 0;
        if (writer != null) {
            try {
                if (bufferSize > 0) {
                    writer =
                        new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(servletOutputStream, getCharacterEncoding()), bufferSize),
                            true);
                } else {
                    writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(servletOutputStream, getCharacterEncoding())), true);
                }
            } catch (final UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public int getOutputSelection() {
        return outputSelection;
    }
}
