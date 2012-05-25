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

package com.openexchange.frontend.uwa.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.lib.console.AdminParser;
import com.openexchange.admin.lib.console.CLIOption;
import com.openexchange.admin.lib.console.ObjectNamingAbstraction;
import com.openexchange.admin.lib.rmi.dataobjects.Context;
import com.openexchange.admin.lib.rmi.dataobjects.Credentials;
import com.openexchange.frontend.uwa.rmi.OXUWAWidgetInterface;
import com.openexchange.frontend.uwa.rmi.Widget;
import com.openexchange.frontend.uwa.rmi.Widget.Field;
import com.openexchange.modules.model.Metadata;

/**
 * {@link UWAWidgetAbstraction}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class UWAWidgetAbstraction extends ObjectNamingAbstraction {

    protected Metadata<Widget> metadata = Widget.METADATA;

    protected AdminParser parser;
    
    protected Context context;

    protected Credentials credentials;

    public UWAWidgetAbstraction(String name, String[] args) {
        try {
            parser = new AdminParser(name);
            setDefaultCommandLineOptions(parser);
            setup();
            parser.ownparse(args);
            parseCommon();
            parse();
            perform(getWidgetInterface());
        } catch (Exception x) {
            printErrors(null, ctxid, x, parser);
            sysexit(1);
        }
    }

    protected abstract void setup() throws Exception;

    protected abstract void parse() throws Exception;

    protected abstract void perform(OXUWAWidgetInterface widgetInterface) throws Exception;

    protected void parseCommon() {
        context = contextparsing(parser);
        credentials = credentialsparsing(parser);
    }

    private static final Map<Field, Character> shortOptions = new EnumMap<Field, Character>(Field.class);
    static {
        shortOptions.put(Field.ID, 'i');
        shortOptions.put(Field.TITLE, 't');
        shortOptions.put(Field.URL, 'u');
        shortOptions.put(Field.STANDALONE, 's');
        shortOptions.put(Field.VISIBLE, 'v');
        shortOptions.put(Field.AUTOREFRESH, 'a');
    }

    private static final Map<Field, String> descriptions = new EnumMap<Field, String>(Field.class);
    static {
        descriptions.put(Field.ID, "The ID of the widget.");
        descriptions.put(Field.TITLE, "The title to display for the widget.");
        descriptions.put(Field.URL, "The URL from which to fetch the widget.");
        descriptions.put(Field.STANDALONE, "Whether this widget runs in standalone mode. Defaults to true.");
        descriptions.put(
            Field.VISIBLE,
            "Whether the widget should be displayed per default for users in the given context. Defaults to true.");
        descriptions.put(Field.AUTOREFRESH, "Whether the widget needs to be refreshed periodically. Defaults to true");
    }

    private static final Map<Field, Object> defaults = new EnumMap<Field, Object>(Field.class);
    static {
        defaults.put(Field.STANDALONE, true);
        defaults.put(Field.VISIBLE, true);
        defaults.put(Field.AUTOREFRESH, true);
    }


    private Map<Field, CLIOption> options = new EnumMap<Field, CLIOption>(Field.class);

    @Override
    protected String getObjectName() {
        return metadata.getName();
    }

    protected void setOptions(AdminParser parser, EnumSet<Field> required, Widget.Field... fields) {
        for (Field field : fields) {
            CLIOption option = setShortLongOpt(
                parser,
                shortOptions.get(field),
                field.getName(),
                field.getName(),
                descriptions.get(field),
                required.contains(field));
            options.put(field, option);
        }
    }

    protected void apply(Widget widget, boolean useDefaults, Widget.Field... fields) {
        for (Field field : fields) {
            CLIOption option = options.get(field);
            Object optionValue = parser.getOptionValue(option);
            if (optionValue == null && useDefaults) {
                optionValue = defaults.get(field);
            }
            if(optionValue != null) {
                widget.set(field, optionValue);
            }
        }
    }
    
    protected void applyDynamicParameters(Widget widget) {
        Map<String, Map<String, String>> dynamicArguments = parser.getDynamicArguments();
        
        Map<String, String> map = dynamicArguments.get("param");
        if(map == null) {
            return;
        }
        for(Map.Entry<String, String> entry : map.entrySet()) {
            widget.setParameter(entry.getKey(), entry.getValue());
        }

    }

    protected final OXUWAWidgetInterface getWidgetInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXUWAWidgetInterface) Naming.lookup(RMI_HOSTNAME + OXUWAWidgetInterface.RMI_NAME);
    }

}
