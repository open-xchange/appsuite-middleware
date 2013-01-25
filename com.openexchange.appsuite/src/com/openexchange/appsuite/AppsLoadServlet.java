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

package com.openexchange.appsuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link AppsLoadServlet} - Provides App Suite data for loading applciations.
 *
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class AppsLoadServlet extends HttpServlet {

    private static final long serialVersionUID = -8909104490806162791L;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AppsLoadServlet.class);

    private static String ZONEINFO = "io.ox/core/date/tz/zoneinfo/";

    private final Map<String, byte[]> cache;

    private final AtomicReference<String> version;

    private final File root, zoneinfo;

    /**
     * Initializes a new {@link AppsLoadServlet}.
     */
    public AppsLoadServlet(final File root, final File zoneinfo) {
        super();
        version = new AtomicReference<String>();
        cache = new ConcurrentHashMap<String, byte[]>();
        this.root = root;
        this.zoneinfo = zoneinfo;
    }

    private static Pattern moduleRE = Pattern.compile("(?:/(text|raw);)?([\\w/-]+(?:\\.[\\w/-]+)*)");

    private static Pattern escapeRE = Pattern.compile("[\\x00-\\x1f'\\\\\\u2028\\u2029]");

    private static String[] escapes = {
        "\\\\x00", "\\\\x01", "\\\\x02", "\\\\x03", "\\\\x04", "\\\\x05", "\\\\x06", "\\\\x07", "\\\\b", "\\\\t", "\\\\n", "\\\\v",
        "\\\\f", "\\\\r", "\\\\x0e", "\\\\x0f", "\\\\x10", "\\\\x11", "\\\\x12", "\\\\x13", "\\\\x14", "\\\\x15", "\\\\x16", "\\\\x17",
        "\\\\x18", "\\\\x19", "\\\\x1a", "\\\\x1b", "\\\\x1c", "\\\\x1d", "\\\\x1e", "\\\\x1f" };

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

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String[] modules = Strings.splitByComma(req.getPathInfo());
        final int length = modules.length;
        if (length < 2) {
            return; // no actual files requested
        }
        // Set version if null or lower than given one
        {
            final String currentVersion = modules[0];
            String version = this.version.get();
            if ((version == null || currentVersion.compareTo(version) > 0) && this.version.compareAndSet(version, currentVersion)) {
                cache.clear();
            }
        }
        resp.setContentType("text/javascript;charset=UTF-8");
        resp.setDateHeader("Expires", System.currentTimeMillis() + (long) 3e10); // + almost a year
        final OutputStream out = resp.getOutputStream();
        for (int i = 1; i < length; i++) {
            final String module = modules[i];

            // Module names may only contain letters, digits, '_', '-', '/' and
            // '.', but not "..".
            final Matcher m = moduleRE.matcher(module);
            if (!m.matches()) {
                final String escapedName = escapeName(module);
                LOG.debug("Invalid module name: '" + escapedName + "'");
                out.write(("console.error('Invalid module name: \\'" + escapedName + "\\'');\n").getBytes("UTF-8"));
                out.flush();
                continue;
            }

            byte[] data = cache.get(module);
            if (data == null) {
                try {
                    data = readFile(module, m.group(1), m.group(2));
                    cache.put(module, data);
                } catch (final IllegalStateException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Illegal path specified.");
                    return;
                }
            }
            out.write(data);
            out.flush();
        }
    }

    @SuppressWarnings("deprecation")
    private byte[] readFile(final String module, final String format, final String name) throws IOException {
        final File filename;
        // Map module name to file name
        if (name.startsWith(ZONEINFO)) {
            filename = new File(zoneinfo, name.substring(ZONEINFO.length()));
        } else {
            filename = new File(root, name);
        }
        LOG.debug("Reading " + filename);

        // Read the entire file's bytes
        final ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(8192);
        {
            InputStream in = null;
            try {
                in = new FileInputStream(filename);
                final int buflen = 2048;
                final byte[] buf = new byte[buflen];
                for (int read = in.read(buf, 0, buflen); read > 0; read = in.read(buf, 0, buflen)) {
                    baos.write(buf, 0, read);
                }
                baos.flush(); // no-op
            } catch (final IOException e) {
                LOG.debug("Could not read from '" + escapeName(filename.getPath()) + "'");
                return ("define('" + escapeName(module) + "', function () { throw new Error(\"Could not read '" + escapeName(name) + "'\"); });\n").getBytes(Charsets.UTF_8);
            } finally {
                Streams.close(in);
            }
        }

        // Special cases for JavaScript-friendly reading of raw files:
        // /text;* returns the file as a UTF-8 string
        // /raw;* maps every byte to [u+0000..u+00ff]
        if (format != null) {
            final StringBuffer sb = new StringBuffer();
            sb.append("define('").append(module).append("','");
            escape("raw".equals(format) ? baos.toString(0) : baos.toString(Charsets.UTF_8_NAME), sb);
            sb.append("');\n");
            return sb.toString().getBytes(Charsets.UTF_8);
        }

        return baos.toByteArray();
    }

}
