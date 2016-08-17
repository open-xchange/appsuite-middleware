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

package com.openexchange.appsuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AppsLoadServlet} - Provides App Suite data for loading applciations.
 *
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class AppsLoadServlet extends SessionServlet {

    private static final long serialVersionUID = -8909104490806162791L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppsLoadServlet.class);

    private static String ZONEINFO = "io.ox/core/date/tz/zoneinfo/";

    // ----------------------------------------------------------------------------------------------------------------------

    private volatile FileCache appCache;
    private volatile FileCache tzCache;
    private final AtomicReference<FileContributor> fileContributorReference;

    /**
     * Initializes a new {@link AppsLoadServlet}.
     *
     * @param contributor The (composite) file contributor.
     * @throws IOException If canonical path names of given files cannot be determined
     */
    public AppsLoadServlet(FileContributor contributor) throws IOException {
        super();
        fileContributorReference = new AtomicReference<FileContributor>(contributor);
    }

    /**
     * Reinitializes this Servlet using given arguments
     *
     * @param roots The app roots
     * @param zoneinfo The zone information
     * @throws IOException If canonical path names of given files cannot be determined
     */
    public synchronized void reinitialize(File[] roots, File zoneinfo) throws IOException {
        appCache = new FileCache(roots);
        tzCache = new FileCache(zoneinfo);
    }

    private String escapeName(String name) {
        if (name.length() > 256) {
            name = name.substring(0, 256);
        }
        final StringBuffer sb = new StringBuffer();
        escape(name, sb);
        return sb.toString();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession if it's missing
        req.getSession(true);
        super.service(req, resp);
    }

    /*
     * Errors must not be cached. Since this is controlled by the "Expires" header at the start, data must be buffered until either an error
     * is found or the end of the data is reached. Since non-error data is cached in RAM anyway, the only overhead is an array of pointers.
     */
    private static class ErrorWriter {

        private boolean buffering = true;
        private final HttpServletResponse resp;
        private OutputStream out;
        private byte[][] buffer;
        private int count = 0;

        ErrorWriter(HttpServletResponse resp, int length) {
            super();
            this.resp = resp;
            this.buffer = new byte[length][];
        }

        private void stopBuffering() throws IOException {
            buffering = false;
            out = resp.getOutputStream();
            for (int i = 0; i < count; i++) {
                write(buffer[i]);
            }
            buffer = null;
        }

        public void write(byte[] data) throws IOException {
            write(data, null);
        }

        public void write(byte[] data, String options) throws IOException {

            if (buffering) {
                data = new StringBuilder(new String(data, "UTF-8")).append("\n\n/* :oxoptions: " + options + " :/oxoptions: */").toString().getBytes("UTF-8");
                buffer[count++] = data;
            } else {
                out.write(data);
                if (options != null) {
                    out.write(("\n// :oxoptions: " + options + " :/oxoptions: \n").getBytes("UTF-8"));
                }
                out.write(SUFFIX);
                out.flush();
            }
        }

        public void error(byte[] data) throws IOException {
            if (buffering) {
                resp.setHeader("Expires", "0");
                stopBuffering();
            }
            write(data);
        }

        public void done() throws IOException {
            if (!buffering) {
                return;
            }
            resp.setDateHeader("Expires", System.currentTimeMillis() + (long) 3e10); // + almost a year
            stopBuffering();
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        ServerSession session = getSessionObject(req, true);

        final String[] modules = Strings.splitByComma(req.getPathInfo());
        if (null == modules) {
            return; // no actual files requested
        }
        final int length = modules.length;
        if (length < 2) {
            return; // no actual files requested
        }
        resp.setContentType("text/javascript;charset=UTF-8");
        ErrorWriter ew = new ErrorWriter(resp, length);
        for (int i = 1; i < length; i++) {
            final String module = modules[i].replace(' ', '+');
            // Module names may only contain letters, digits, '_', '-', '/' and
            // '.', but not "..".
            final Matcher m = moduleRE.matcher(module);
            if (!m.matches()) {
                final String escapedName = escapeName(module);
                LOG.debug("Invalid module name: '{}'", escapedName);
                ew.error(("console.error('Invalid module name detected');\n").getBytes("UTF-8"));
                continue;
            }

            // Map module name to file name
            final String format = m.group(1);
            String name = m.group(2);
            boolean isTZ = name.startsWith(ZONEINFO);
            final String resolved = isTZ ? name.substring(ZONEINFO.length()) : name;

            FileCache cache = isTZ ? tzCache : appCache;
            byte[] data = cache.get(module, new FileCache.Filter() {

                @Override
                public String resolve(String path) {
                    return resolved;
                }

                @Override
                @SuppressWarnings("deprecation")
                public byte[] filter(ByteArrayOutputStream baos) {
                    if (format == null) {
                        return baos.toByteArray();
                    }

                    // Special cases for JavaScript-friendly reading of raw files:
                    // /text;* returns the file as a UTF-8 string
                    // /raw;* maps every byte to [u+0000..u+00ff]
                    final StringBuffer sb = new StringBuffer();
                    sb.append("define('").append(module).append("','");
                    try {
                        escape("raw".equals(format) ? baos.toString(0) : baos.toString(Charsets.UTF_8_NAME), sb);
                    } catch (UnsupportedEncodingException e) {
                        // everybody should have UTF-8
                    }
                    sb.append("');\n");
                    return sb.toString().getBytes(Charsets.UTF_8);
                }
            });

            if (data != null) {
                ew.write(data);
            } else {
                // Try external sources
                String options = "{}";
                FileContributor contributors = fileContributorReference.get();
                if (contributors != null) {
                    try {
                        FileContribution contribution = contributors.getData(ServerSessionAdapter.valueOf(session), module);
                        if (contribution != null) {
                            data = contribution.getData();
                            JSONObject optionsO = new JSONObject();
                            try {
                                optionsO.put("cache", !contribution.isCachingDisabled());
                                options = optionsO.toString();
                            } catch (JSONException e) {
                                // Doesn't happen
                            }
                        }
                    } catch (OXException e) {
                        int len = module.length() - 3;
                        String moduleName = module;
                        if (format == null && len > 0 && ".js".equals(module.substring(len))) {
                            moduleName = module.substring(0, len);
                        }
                        name = escapeName(name);
                        ew.error(("define('" + escapeName(moduleName) + "', function () {\n" +
                                  "    if (ox.debug) console.log(\"Could not read '" + name + "': " + e.toString() + "\");\n" +
                                  "    throw new Error(\"Could not read '" + name + "'\");\n" +
                                  "});\n").getBytes(Charsets.UTF_8));
                    }
                }
                if (data != null) {
                    ew.write(data, options);
                } else {
                    int len = module.length() - 3;
                    String moduleName = module;
                    if (format == null && len > 0 && ".js".equals(module.substring(len))) {
                        moduleName = module.substring(0, len);
                    }
                    name = escapeName(name);
                    ew.error(("define('" + escapeName(moduleName) + "', function () {\n" +
                              "    if (ox.debug) console.log(\"Could not read '" + name + "'\");\n" +
                              "    throw new Error(\"Could not read '" + name + "'\");\n" +
                              "});\n").getBytes(Charsets.UTF_8));
                }
            }
        }
        ew.done();
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static Pattern moduleRE = Pattern.compile("(?:/(text|raw);)?([\\w/+-]+(?:\\.[\\w/+-]+)*)");

    private static Pattern escapeRE = Pattern.compile("[\\x00-\\x1f'\\\\\\u2028\\u2029]");

    private static String[] escapes = {
        "\\\\x00", "\\\\x01", "\\\\x02", "\\\\x03", "\\\\x04", "\\\\x05", "\\\\x06", "\\\\x07", "\\\\b", "\\\\t", "\\\\n", "\\\\v",
        "\\\\f", "\\\\r", "\\\\x0e", "\\\\x0f", "\\\\x10", "\\\\x11", "\\\\x12", "\\\\x13", "\\\\x14", "\\\\x15", "\\\\x16", "\\\\x17",
        "\\\\x18", "\\\\x19", "\\\\x1a", "\\\\x1b", "\\\\x1c", "\\\\x1d", "\\\\x1e", "\\\\x1f" };

    private static byte[] SUFFIX;
    static {
        try {
            SUFFIX = "\n/*:oxsep:*/\n".getBytes(Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            SUFFIX = "\n/*:oxsep:*/\n".getBytes();
        }
    }

    private static void escape(final CharSequence s, final StringBuffer sb) {
        final Matcher e = escapeRE.matcher(s);
        while (e.find()) {
            final int chr = e.group().codePointAt(0);
            String replacement;
            switch (chr) {
            case 0x27:
                replacement = "\\\\'";
                break;
            case 0x5c:
                replacement = "\\\\\\\\";
                break;
            case 0x2028:
                replacement = "\\\\u2028";
                break;
            case 0x2029:
                replacement = "\\\\u2029";
                break;
            default:
                replacement = escapes[chr];
            }
            e.appendReplacement(sb, replacement);
        }
        e.appendTail(sb);
    }

}
