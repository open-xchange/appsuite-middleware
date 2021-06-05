/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        "onmouseleave",
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
        "onrejectionhandled",
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

        "onunhandledrejection",
        "onunload",

        "onvolumechange",

        "onwaiting",
        "onwheel",

        "onzoom"
        );

}
