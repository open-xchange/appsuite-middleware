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

package com.openexchange.html.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlExceptionCodes;
import com.openexchange.html.osgi.Services;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link GlobalEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class GlobalEventHandler implements Reloadable {

    private static final GlobalEventHandler INSTANCE = new GlobalEventHandler();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static GlobalEventHandler getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------

    private final AtomicReference<List<String>> handlerListReference;

    /**
     * Initializes a new {@link GlobalEventHandler}.
     */
    private GlobalEventHandler() {
        super();
        handlerListReference = new AtomicReference<List<String>>(null);
    }

    /**
     * Gets a listing of global event handler identifiers.
     *
     * @return A listing of global event handler identifiers
     */
    public List<String> getGlobalEventHandlerIdentifiers() {
        List<String> list = handlerListReference.get();
        if (null == list) {
            synchronized (handlerListReference) {
                list = handlerListReference.get();
                if (null == list) {
                    List<String> defaultList = DEFAULT_LIST;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultList;
                    }

                    File file = service.getFileByName("globaleventhandlers.list");
                    if (null == file) {
                        list = DEFAULT_LIST;
                    } else {
                        try {
                            list = readGlobalEventHandlerFile(file, defaultList);
                        } catch (OXException e) {
                            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalEventHandler.class);
                            logger.warn("Failed to read global event handlers. Using default list instead.", e);
                            list = defaultList;
                        }
                    }
                    handlerListReference.set(list);
                }
            }
        }
        return list;
    }

    private List<String> readGlobalEventHandlerFile(File file, List<String> defaultList) throws OXException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.ISO_8859_1));
            Set<String> set = new TreeSet<String>(defaultList);
            for (String line = reader.readLine(); null != line; line = reader.readLine()) {
                line = line.trim();
                if (Strings.isNotEmpty(line) && '#' != line.charAt(0)) {
                    set.add(Strings.asciiLowerCase(line));
                }
            }
            return ImmutableList.copyOf(set);
        } catch (IOException e) {
            throw HtmlExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException rte) {
            throw HtmlExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    // ------------------------------------------------------------------------------------------

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("globaleventhandlers.list");
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        handlerListReference.set(null);
    }

    // ------------------------------------------------------------------------------------------

    private static final List<String> DEFAULT_LIST = ImmutableList.of(
        "onabort",
        "onactivate",
        "onafterprint",
        "onanimationend",
        "onanimationiteration",
        "onanimationstart",

        "onbeforeprint",
        "onbeforeunload",
        "onblur",

        "oncanplay",
        "oncanplaythrough",
        "onchange",
        "onclick ",
        "oncontextmenu",
        "oncopy",
        "oncuechange",
        "oncut",

        "ondblclick",
        "ondomcontentloaded",
        "ondrag",
        "ondragend",
        "ondragenter",
        "ondragleave",
        "ondragover",
        "ondragstart",
        "ondrop",
        "ondurationchange",

        "onemptied",
        "onended",
        "onerror",

        "onfocus",
        "onfocusin",
        "onfocusout",

        "ongotpointercapture",

        "onhashchange",

        "oninput",
        "oninvalid",

        "onjavascript",

        "onkeydown",
        "onkeypress",
        "onkeyup",

        "onlanguagechange",
        "onload",
        "onloadeddata",
        "onloadedmetadata",
        "onloadstart",
        "onlostpointercapture",

        "onmessage",
        "onmousedown",
        "onmouseenter",
        "onmounseleave",
        "onmousemove",
        "onmouseout",
        "onmouseover",
        "onmouseup",
        "onmousewheel",

        "onoffline",
        "ononline",

        "onpageshow",
        "onpagehide",
        "onpaste",
        "onpause",
        "onplay",
        "onplaying",
        "onpointercancel",
        "onpointerdown",
        "onpointerenter",
        "onpointerleave",
        "onpointermove",
        "onpointerout",
        "onpointerover",
        "onpointerup",
        "onpopstate",
        "onprogress",

        "onratechange",
        "onreset",
        "onresize",

        "onscroll",
        "onsearch",
        "onseeked",
        "onseeking",
        "onselect",
        "onshow",
        "onstalled",
        "onstorage",
        "onsubmit",
        "onsuspend",

        "ontimeupdate",
        "ontoggle",
        "ontouchcancel",
        "ontouchend",
        "ontouchmove",
        "ontouchstart",
        "ontransitioned",

        "onunload",

        "onvolumechange",

        "onwaiting",
        "onwheel",

        "onzoom"
        );

}
