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

package com.openexchange.ui7;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Viktor Pracht <viktor.pracht@open-xchange.com>
 */
public class AppsServlet extends HttpServlet {

    private static final long serialVersionUID = -8909104490806162791L;

    private static org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AppsServlet.class));

    private Map<String, byte[]> cache = new ConcurrentHashMap<String, byte[]>();

    private String version;

    private File prefix;

    public AppsServlet(String path) {
        if (!path.endsWith("/"))
            path += "/";
        path += "apps/";
        prefix = new File(path);
    }

    private static Pattern moduleRE = Pattern.compile("(?:/(text|raw);)?([\\w/-]+(?:\\.[\\w/-]+)*)");

    private static Pattern escapeRE = Pattern.compile("[\\x00-\\x1f'\\\\\\u2028\\u2029]");

    private static String[] escapes = {
        "\\\\x00", "\\\\x01", "\\\\x02", "\\\\x03", "\\\\x04", "\\\\x05", "\\\\x06", "\\\\x07", "\\\\b", "\\\\t", "\\\\n", "\\\\v",
        "\\\\f", "\\\\r", "\\\\x0e", "\\\\x0f", "\\\\x10", "\\\\x11", "\\\\x12", "\\\\x13", "\\\\x14", "\\\\x15", "\\\\x16", "\\\\x17",
        "\\\\x18", "\\\\x19", "\\\\x1a", "\\\\x1b", "\\\\x1c", "\\\\x1d", "\\\\x1e", "\\\\x1f" };

    private static void escape(CharSequence s, StringBuffer sb) {
        Matcher e = escapeRE.matcher(s);
        while (e.find()) {
            int chr = e.group().codePointAt(0);
            String replacement;
            switch (chr) {
            case 0x27:
                replacement = "\\\\'";
                break;
            case 0x5c:
                replacement = "\\\\\\\\";
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] modules = req.getPathInfo().split(",");
        if (modules.length < 2)
            return;
        if (version == null || modules[0].compareTo(version) > 0) {
            version = modules[0];
            cache.clear();
            LOG.info("Started serving version " + version);
        }
        resp.setContentType("text/javascript;charset=UTF-8");
        OutputStream out = resp.getOutputStream();
        for (int i = 1; i < modules.length; i++) {
            String module = modules[i];
            Matcher m = moduleRE.matcher(module);
            if (!m.matches()) {
                LOG.info("Invalid module name: " + module);
                StringBuffer sb = new StringBuffer();
                sb.append("console.error('Invalid module name: \"");
                escape(module, sb);
                sb.append("\"');\n");
                out.write(sb.toString().getBytes("UTF-8"));
                continue;
            }
            byte[] data = cache.get(module);
            if (data == null) {
                String name = m.group(2);
                File filename = new File(prefix, name);
                LOG.info("Reading " + filename);
                try {
                    RandomAccessFile f = new RandomAccessFile(filename, "r");
                    data = new byte[(int) f.length()];
                    f.readFully(data);
                    f.close();
                    if (m.group(1) != null) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("define('").append(module).append("','");
                        String payload;
                        if ("raw".equals(m.group(1))) {
                            StringBuffer raw = new StringBuffer();
                            for (byte b : data)
                                raw.append((char) (b & 255));
                            payload = raw.toString();
                        } else {
                            payload = new String(data, "UTF-8");
                        }
                        escape(payload, sb);
                        sb.append("');\n");
                        data = sb.toString().getBytes("UTF-8");
                    }
                } catch (IOException e) {
                    LOG.info("Could not read " + filename);
                    data = ("console.error('Could not read " + module + "');\n").getBytes("UTF-8");
                }
                cache.put(module, data);
            }
            out.write(data);
        }
    }
}
