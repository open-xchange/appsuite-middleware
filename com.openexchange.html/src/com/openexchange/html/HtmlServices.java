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

package com.openexchange.html;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.isWhitespace;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.net.URLCodec;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.internal.WhitelistedSchemes;
import com.openexchange.html.osgi.Services;
import net.htmlparser.jericho.HTMLElementName;


/**
 * {@link HtmlServices} - A utility class for {@link HtmlService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlServices {

    public static void main(String[] args) {
        boolean nonJavaScriptURL = isNonJavaScriptURL("java&#09;script:alert(document.domain)", null);

        System.out.println(nonJavaScriptURL);
    }

    /**
     * Initializes a new {@link HtmlServices}.
     */
    private HtmlServices() {
        super();
    }

    private static final Pattern UNICODE_CHAR = Pattern.compile("&(?:amp;)?#0*([1-9][0-9]*);?", Pattern.CASE_INSENSITIVE);

    /**
     * Does URL decoding until fully decoded
     *
     * @param sInput The input to sanitize, can be <code>null</code> or empty
     * @return The fully decoded string
     */
    public static String fullUrlDecode(String sInput) {
        if (isEmpty(sInput)) {
            return sInput;
        }

        String s = sInput;

        // Do URL decoding until fully decoded
        {
            int pos = s.indexOf('%');
            if (pos >= 0 && pos < s.length() - 1) {
                URLCodec urlCodec = new URLCodec("UTF-8");
                boolean k = true;
                do {
                    try {
                        s = urlCodec.decode(s);
                    } catch (org.apache.commons.codec.DecoderException e) {
                        // Break...
                        k = false;
                    }
                } while (k && (pos = s.indexOf('%')) >= 0 && pos < s.length() - 1);
            }
        }

        if (s.indexOf('#') >= 0) {
            for (Matcher m; (m = UNICODE_CHAR.matcher(s)).find();) {
                StringBuffer sb = new StringBuffer(s.length());
                do {
                    char c = (char) Integer.parseInt(m.group(1));
                    m.appendReplacement(sb, String.valueOf(c));
                } while (m.find());
                m.appendTail(sb);
                s = sb.toString();
            }
        }

        // Return result
        return s;
    }

    /**
     * Checks if specified URL String is safe or not.
     *
     * @param val The URL String to check
     * @param tagName The name of the tag
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isNonJavaScriptURL(String val, String tagName) {
        return isNonJavaScriptURL(val, tagName, new String[0]);
    }

    private static final String DATA_TOKEN = "data:";

    /**
     * Checks if specified value is an acceptable data URI.
     *
     * @param value The value to check
     * @param condition The optional condition that is supposed to be satisfied
     * @return <code>Result.NEUTRAL</code> if given value is no data URI, <code>Result.ALLOW</code> if acceptable; otherwise <code>Result.DENY</code> if not
     */
    public static Result isAcceptableDataUri(String value, Callable<Boolean> condition) {
        String val = value.trim();
        if (false == asciiLowerCase(val).startsWith(DATA_TOKEN)) {
            // No data URI at all
            return Result.NEUTRAL;
        }

        // Assume data URL  data:[<media type>][;base64],<data>
        //            E.g. "data:image/jpg;base64,/9j/7gAOQWRvYmUAZMAAAAAB/..."

        // Check condition (if any)
        if (null != condition) {
            try {
                if (false == Boolean.TRUE.equals(condition.call())) {
                    // Condition not satisfied
                    return Result.DENY;
                }
            } catch (Exception e) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HtmlServices.class);
                logger.error("Failed to check condition", e);
                return Result.DENY;
            }
        }

        int dataPos = DATA_TOKEN.length();
        int commaPos = val.indexOf(',', dataPos);
        if (commaPos < 0) {
            // Impossible to parse MIME type. Deny.
            return Result.DENY;
        }

        // Allow for image only
        int endPos = val.indexOf(';', dataPos);
        if (endPos < 0) {
            endPos = commaPos;
        }
        String mimeType = asciiLowerCase(val.substring(dataPos, endPos).trim());
        return (mimeType.startsWith("image/") && mimeType.indexOf("svg") < 0) ? Result.ALLOW : Result.DENY;
    }

    private static final String[] UNSAFE_TOKENS = { "javascript:", "vbscript:", "<script" };

    /**
     * Checks if specified URL String is safe or not.
     *
     * @param val The URL String to check
     * @param tagName The name of the tag
     * @param more More tokens to look for
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isNonJavaScriptURL(String val, final String tagName, String... more) {
        if (null == val) {
            return false;
        }

        // Check for acceptable data URI inside <img> tag
        String lc = asciiLowerCase(fullUrlDecode(val.trim()));
        {
            Callable<Boolean> condition = new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return Boolean.valueOf(HTMLElementName.IMG == tagName || "img".equals(tagName));
                }
            };
            if (Result.DENY == isAcceptableDataUri(val, condition)) {
                return false;
            }
        }

        // Check basic unsafe tokens
        lc = dropWhitespacesFrom(lc);
        for (String unsafeToken : UNSAFE_TOKENS) {
            if (lc.indexOf(unsafeToken) >= 0) {
                return false;
            }
        }

        // Check additionally specified unsafe tokens
        if (null != more && more.length > 0) {
            for (final String token : more) {
                if (lc.indexOf(asciiLowerCase(token)) >= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String dropWhitespacesFrom(String str) {
        if (null == str) {
            return null;
        }

        int length = str.length();
        StringBuilder sb = null;
        for (int k = length, i = 0; k-- > 0; i++) {
            char c = str.charAt(i);
            if (isWhitespace(c)) {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str.substring(0, i));
                    }
                }
            } else {
                if (null != sb) {
                    sb.append(c);
                }
            }
        }

        return null == sb ? str : sb.toString();
    }

    /**
     * Checks if specified URL String is unsafe or not.
     *
     * @param val The URL String to check
     * @param tagName The name of the tag
     * @return <code>true</code> if unsafe; otherwise <code>false</code>
     */
    public static boolean isJavaScriptURL(String val, String tagName) {
        return !isNonJavaScriptURL(val, tagName);
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Checks given possible URI it is safe being appended/inserted to HTML content.
     *
     * @param possibleUrl The possible URI to check
     * @param tagName The name of the tag
     * @return <code>true</code> if safe; otherwise <code>false</code>
     */
    public static boolean isSafe(String possibleUrl, String tagName) {
        if (null == possibleUrl) {
            return true;
        }

        // Check for possible URI
        URI uri;
        try {
            uri = new URI(possibleUrl.trim());
        } catch (URISyntaxException x) {
            // At least check for common attack vectors
            return isNonJavaScriptURL(possibleUrl, tagName);
        }

        // Get URI's scheme and compare against possible whitelist
        String scheme = uri.getScheme();
        if (null == scheme) {
            // No scheme...
            return true;
        }

        List<String> schemes = WhitelistedSchemes.getWhitelistedSchemes();
        if (schemes.isEmpty()) {
            // No allowed schemes specified
            return isNonJavaScriptURL(possibleUrl, tagName);
        }

        String lc = asciiLowerCase(scheme);
        for (String s : schemes) {
            if (lc.equals(s)) {
                // Matches an allowed scheme
                return isNonJavaScriptURL(possibleUrl, tagName);
            }
        }

        // Not allowed...
        return false;
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    /** Volatile cache variable for HTML size threshold */
    private static volatile Integer maxLength;

    /**
     * Gets the HTML size threshold (<code>"<i>com.openexchange.html.maxLength</i>"</code> property).
     *
     * @return The HTML size threshold
     */
    public static int htmlThreshold() {
        Integer i = maxLength;
        if (null == maxLength) {
            synchronized (HtmlServices.class) {
                i = maxLength;
                if (null == maxLength) {
                    // Default is 1MB
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    int defaultMaxLength = 1048576;
                    if (null == service) {
                        return defaultMaxLength;
                    }
                    i = Integer.valueOf(service.getIntProperty("com.openexchange.html.maxLength", defaultMaxLength));
                    maxLength = i;
                }
            }
        }
        return i.intValue();
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private static char[] lowercases = {
        '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017',
        '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037',
        '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057',
        '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
        '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137',
        '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    public static String asciiLowerCase(String s) {
        if (null == s) {
            return null;
        }

        char[] c = null;
        int i = s.length();

        // look for first conversion
        while (i-- > 0) {
            char c1 = s.charAt(i);
            if (c1 <= 127) {
                char c2 = lowercases[c1];
                if (c1 != c2) {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            }
        }

        while (i-- > 0) {
            if (c[i] <= 127) {
                c[i] = lowercases[c[i]];
            }
        }

        return c == null ? s : new String(c);
    }

}
