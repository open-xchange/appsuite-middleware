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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link BrowserDetector} - Parses useful information out of <i>"user-agent"</i> header.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BrowserDetector {

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

    /**
     * Initializes a new {@link BrowserDetector}.
     * 
     * @param userAgent The user-agent
     */
    public BrowserDetector(final String userAgent) {
        this.userAgent = userAgent;
        parse();
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
            browserName = "unknown";
            browserVersion = -1F;
            browserPlatform = "unknown";
            return;
        }
        int versionStartIndex = userAgent.indexOf("/");
        int versionEndIndex = userAgent.indexOf(" ");

        // Get the browser name and version.
        browserName = userAgent.substring(0, versionStartIndex);
        try {
            // Not all user agents will have a space in the reported
            // string.
            String agentSubstring = null;
            if (versionEndIndex < 0) {
                agentSubstring = userAgent.substring(versionStartIndex + 1);
            } else {
                agentSubstring = userAgent.substring(versionStartIndex + 1, versionEndIndex);
            }
            browserVersion = toFloat(agentSubstring);
        } catch (final NumberFormatException e) {
            // Just use the default value.
        }

        // MSIE lies about its name. Of course...
        if (userAgent.indexOf(MSIE) != -1) {
            // Ex: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)
            versionStartIndex = (userAgent.indexOf(MSIE) + MSIE.length() + 1);
            versionEndIndex = userAgent.indexOf(";", versionStartIndex);

            browserName = MSIE;
            try {
                browserVersion = toFloat(userAgent.substring(versionStartIndex, versionEndIndex));
            } catch (final NumberFormatException e) {
                // Just use the default value.
            }

            // PHP code
            // $Browser_Name = "MSIE";
            // $Browser_Version = strtok("MSIE");
            // $Browser_Version = strtok(" ");
            // $Browser_Version = strtok(";");
        }

        // Opera isn't completely honest, either...
        // Modificaton by Chris Mospaw <mospaw@polk-county.com>
        if (userAgent.indexOf(OPERA) != -1) {
            // Ex: Mozilla/4.0 (Windows NT 4.0;US) Opera 3.61 [en]
            versionStartIndex = (userAgent.indexOf(OPERA) + OPERA.length() + 1);
            versionEndIndex = userAgent.indexOf(" ", versionStartIndex);

            browserName = OPERA;
            try {
                browserVersion = toFloat(userAgent.substring(versionStartIndex, versionEndIndex));
            } catch (final NumberFormatException e) {
                // Just use the default value.
            }

            // PHP code
            // $Browser_Name = "Opera";
            // $Browser_Version = strtok("Opera");
            // $Browser_Version = strtok("/");
            // $Browser_Version = strtok(";");
        }

        // Try to figure out what platform.
        if ((userAgent.indexOf("Windows") != -1) || (userAgent.indexOf("WinNT") != -1) || (userAgent.indexOf("Win98") != -1) || (userAgent.indexOf("Win95") != -1)) {
            browserPlatform = WINDOWS;
        }

        if (userAgent.indexOf("Mac") != -1) {
            browserPlatform = MACINTOSH;
        }

        if (userAgent.indexOf("X11") != -1) {
            browserPlatform = UNIX;
        }

        if (browserPlatform == WINDOWS) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0) {
                    cssOK = true;
                }
            } else if (browserName == MSIE) {
                if (browserVersion >= 4.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                    cssOK = true;
                }
            } else if (browserName == OPERA) {
                if (browserVersion >= 3.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                    cssOK = true;
                }
            }
        } else if (browserPlatform == MACINTOSH) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0) {
                    cssOK = true;
                }
            } else if (browserName == MSIE) {
                if (browserVersion >= 4.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion > 4.0) {
                    cssOK = true;
                }
            }
        } else if (browserPlatform == UNIX) {
            if (browserName.equals(MOZILLA)) {
                if (browserVersion >= 3.0) {
                    javascriptOK = true;
                    fileUploadOK = true;
                }
                if (browserVersion >= 4.0) {
                    cssOK = true;
                }
            }
        }
    }

    /**
     * Helper method to convert String to a float.
     * 
     * @param s A String.
     * @return The String converted to float.
     */
    private static float toFloat(final String s) {
        return Float.valueOf(s).floatValue();
    }

    @Override
    public String toString() {
        if (getBrowserName().equals(BrowserDetector.MOZILLA)) {
            return BrowserDetector.MOZILLA;
        }
        return BrowserDetector.MSIE;
    }

}
