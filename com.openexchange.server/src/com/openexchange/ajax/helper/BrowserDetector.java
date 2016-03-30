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

package com.openexchange.ajax.helper;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * {@link BrowserDetector} - Parses useful information out of <i>"user-agent"</i> header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BrowserDetector {

    private static final Cache<String, BrowserDetector> CACHE = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(2, TimeUnit.HOURS).build();

    /**
     * Gets the {@link BrowserDetector} instance for given User-Agent string.
     *
     * @param userAgent The User-Agent string
     * @return The {@link BrowserDetector} instance
     */
    public static BrowserDetector detectorFor(String userAgent) {
        if (null == userAgent) {
            return null;
        }

        BrowserDetector result = CACHE.getIfPresent(userAgent);
        if (result == null) {
            result = new BrowserDetector(userAgent);
            CACHE.put(userAgent, result);
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * The constant for an unknown value.
     */
    public static final String UNKNOWN = "unknown";

    /**
     * Identifier for <i>Internet Explorer</i> browser.
     */
    public static final String MSIE = "MSIE";

    /**
     * Identifier for <i>Opera</i> browser.
     */
    public static final String OPERA = "Opera";

    /**
     * Identifier for <i>Mozilla</i> browser.
     */
    public static final String MOZILLA = "Mozilla";

    /**
     * Identifier for <i>Windows</i> platform.
     */
    public static final String WINDOWS = "Windows";

    /**
     * Identifier for <i>Unix</i> platform.
     */
    public static final String UNIX = "Unix";

    /**
     * Identifier for <i>Macintosh</i> platform.
     */
    public static final String MACINTOSH = "Macintosh";

    /*-
     * Member section
     */

    /**
     * The user agent string.
     */
    private final String userAgent;

    /**
     * The browser name specified in the user agent string.
     */
    private String browserName;

    /**
     * The browser version specified in the user agent string. If we can't parse the version just assume an old browser.
     */
    private float browserVersion;

    /**
     * The browser platform specified in the user agent string.
     */
    private String browserPlatform;

    /**
     * Whether or not javascript works in this browser.
     */
    private boolean javascriptOK;

    /**
     * Whether or not CSS works in this browser.
     */
    private boolean cssOK;

    /**
     * Whether or not file upload works in this browser.
     */
    private boolean fileUploadOK;

    private boolean safari;

    /**
     * Initializes a new {@link BrowserDetector}.
     *
     * @param userAgent The user-agent
     * @deprecated Use {@link #detectorFor(String)} method
     */
    @Deprecated
    public BrowserDetector(String userAgent) {
        super();
        this.userAgent = userAgent;
        browserName = UNKNOWN;
        browserVersion = 0F;
        browserPlatform = UNKNOWN;
        parse();
    }

    /**
     * Checks if specified instance is equal by: browser name, browser platform and version.
     *
     * @param other The instance to compare with
     * @return <code>true</code> if specified instance is equal by: browser name, browser platform and version; otherwise <code>false</code>
     */
    public boolean nearlyEquals(final BrowserDetector other) {
        if (!browserName.equals(other.browserName)) {
            return false;
        }
        if (!browserPlatform.equals(other.browserPlatform)) {
            return false;
        }
        if (browserVersion != other.browserVersion) {
            return false;
        }
        return true;
    }

    /**
     * Whether or not CSS works in this browser.
     *
     * @return True if CSS works in this browser.
     */
    public boolean isCssOK() {
        return cssOK;
    }

    /**
     * Whether or not file upload works in this browser.
     *
     * @return True if file upload works in this browser.
     */
    public boolean isFileUploadOK() {
        return fileUploadOK;
    }

    /**
     * Whether or not Javascript works in this browser.
     *
     * @return True if Javascript works in this browser.
     */
    public boolean isJavascriptOK() {
        return javascriptOK;
    }

    /**
     * The browser name specified in the user agent string.
     *
     * @return A String with the browser name.
     */
    public String getBrowserName() {
        return browserName;
    }

    /**
     * The browser platform specified in the user agent string.
     *
     * @return A String with the browser platform.
     */
    public String getBrowserPlatform() {
        return browserPlatform;
    }

    /**
     * The browser version specified in the user agent string.
     *
     * @return A String with the browser version.
     */
    public float getBrowserVersion() {
        return browserVersion;
    }

    /**
     * The user agent string for this class.
     *
     * @return A String with the user agent.
     */
    public String getUserAgentString() {
        return userAgent;
    }

    /**
     * Checks if user-agent indicates Internet Explorer browser.
     *
     * @return <code>true</code> if user-agent indicates Internet Explorer browser; otherwise <code>false</code>
     */
    public boolean isMSIE() {
        return MSIE.equals(getBrowserName());
    }

    public boolean isSafari() {
        return safari;
    }

    public boolean isSafari5() {
        return safari && browserVersion >= 5.0F;
    }

    /**
     * Checks if user-agent indicates Windows platform.
     *
     * @return <code>true</code> if user-agent indicates Windows platform; otherwise <code>false</code>
     */
    public boolean isWindows() {
        return WINDOWS.equals(getBrowserPlatform());
    }

    /**
     * Parses the user-agent.
     */
    private void parse() {
        if (null == userAgent || userAgent.length() == 0) {
            return;
        }
        int versionStartIndex = userAgent.indexOf('/');
        if (versionStartIndex < 0) {
            // Not a valid user-agent string
            browserName = userAgent;
            return;
        }
        int versionEndIndex = userAgent.indexOf(' ', versionStartIndex);

        /*
         * Get the browser name and version.
         */
        browserName = userAgent.substring(0, versionStartIndex);
        try {
            /*
             * Not all user agents will have a space in the reported string.
             */
            browserVersion =
                Float.parseFloat(versionEndIndex < 0 ? userAgent.substring(versionStartIndex + 1) : userAgent.substring(
                    versionStartIndex + 1,
                    versionEndIndex));
        } catch (final NumberFormatException e) {
            /*
             * Just use the default value.
             */
            browserVersion = 0F;
        }

        int index = -1;
        /*
         * MSIE lies about its name. Of course...
         */
        if ((index = userAgent.indexOf(MSIE)) >= 0) {
            /*
             * Ex: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)
             */
            versionStartIndex = (index + MSIE.length() + 1);
            versionEndIndex = userAgent.indexOf(';', versionStartIndex);

            browserName = MSIE;
            try {
                browserVersion = Float.parseFloat(userAgent.substring(versionStartIndex, versionEndIndex));
            } catch (final NumberFormatException e) {
                /*
                 * Just use the default value.
                 */
                browserVersion = 0F;
            }

            // PHP code
            // $Browser_Name = "MSIE";
            // $Browser_Version = strtok("MSIE");
            // $Browser_Version = strtok(" ");
            // $Browser_Version = strtok(";");
        }

        /*
         * Opera isn't completely honest, either... Modificaton by Chris Mospaw <mospaw@polk-county.com>
         */
        if ((index = userAgent.indexOf(OPERA)) >= 0) {
            /*
             * Ex: Mozilla/4.0 (Windows NT 4.0;US) Opera 3.61 [en]
             */
            versionStartIndex = (index + OPERA.length() + 1);
            versionEndIndex = userAgent.indexOf(' ', versionStartIndex);

            browserName = OPERA;
            try {
                browserVersion = Float.parseFloat(userAgent.substring(versionStartIndex, versionEndIndex));
            } catch (final NumberFormatException e) {
                /*
                 * Just use the default value.
                 */
                browserVersion = 0F;
            }

            // PHP code
            // $Browser_Name = "Opera";
            // $Browser_Version = strtok("Opera");
            // $Browser_Version = strtok("/");
            // $Browser_Version = strtok(";");
        }

        /*
         * Try to figure out what platform.
         */
        if ((userAgent.indexOf("Windows") >= 0) || (userAgent.indexOf("WinNT") >= 0) || (userAgent.indexOf("Win98") >= 0) || (userAgent.indexOf("Win95") >= 0)) {
            browserPlatform = WINDOWS;
        }

        if (userAgent.indexOf("Mac") >= 0) {
            browserPlatform = MACINTOSH;
        }

        if (userAgent.indexOf("X11") >= 0) {
            browserPlatform = UNIX;
        }

        if (WINDOWS.equals(browserPlatform)) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0F) {
                    cssOK = true;
                }
            } else if (browserName.equals(MSIE)) {
                if (browserVersion >= 4.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                    cssOK = true;
                }
            } else if (browserName.equals(OPERA)) {
                if (browserVersion >= 3.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                    cssOK = true;
                }
            }
        } else if (MACINTOSH.equals(browserPlatform)) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0F) {
                    cssOK = true;
                }
            } else if (browserName.equals(MSIE)) {
                if (browserVersion >= 4.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion > 4.0F) {
                    cssOK = true;
                }
            }
        } else if (UNIX.equals(browserPlatform)) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0F) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0F) {
                    cssOK = true;
                }
            }
        }
        if (userAgent.contains("Safari")) {
            safari = true;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128).append("User agent=").append(userAgent);
        sb.append("\nBrowser name=").append(browserName).append(", browser version=").append(browserVersion);
        sb.append(", browser platform=").append(browserPlatform);
        return sb.toString();
    }

}
