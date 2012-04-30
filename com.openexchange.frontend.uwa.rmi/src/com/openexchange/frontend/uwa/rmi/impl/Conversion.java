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

package com.openexchange.frontend.uwa.rmi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.frontend.uwa.UWAWidget;
import com.openexchange.frontend.uwa.UWAWidget.Field;
import com.openexchange.frontend.uwa.rmi.Widget;
import com.openexchange.java.Autoboxing;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.AttributeHandler;
import com.openexchange.modules.model.Tools;

/**
 * {@link Conversion}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Conversion {

    static final Log LOG = LogFactory.getLog(Conversion.class);

    private Widget widget;

    private UWAWidget uwaWidget;

    private List<Field> modifiedFields = new ArrayList<Field>(Field.values().length);

    public Conversion(Widget widget) {
        this.widget = widget;
        initUWAWidget();
    }

    public Conversion(UWAWidget uwaWidget) {
        this.uwaWidget = uwaWidget;
        initWidget();
    }

    
    public Widget getWidget() {
        return widget;
    }

    
    public UWAWidget getUWAWidget() {
        return uwaWidget;
    }

    
    public List<Field> getModifiedFields() {
        return modifiedFields;
    }

    private void initWidget() {
        this.widget = new Widget();
        Tools.each(uwaWidget, new Setter(widget), Arrays.asList(UWAWidget.Field.values()));
    }

    private void initUWAWidget() {
        this.uwaWidget = new UWAWidget();

        if (widget.isIdModified()) {
            uwaWidget.setId(widget.getId());
            modifiedFields.add(UWAWidget.Field.ID);
        }
        if (widget.isTitleModified()) {
            uwaWidget.setTitle(widget.getTitle());
            modifiedFields.add(UWAWidget.Field.TITLE);
        }
        if (widget.isURLModified()) {
            uwaWidget.setURL(widget.getURL());
            modifiedFields.add(UWAWidget.Field.ID);
        }
        if (widget.isAutorefreshModified()) {
            uwaWidget.setAutorefresh(widget.getAutorefresh());
            modifiedFields.add(UWAWidget.Field.AUTOREFRESH);
        }
        if (widget.isStandaloneModified()) {
            uwaWidget.setStandalone(widget.isStandalone());
            modifiedFields.add(UWAWidget.Field.STANDALONE);
        }
        if (widget.isVisibleModified()) {
            uwaWidget.setVisible(widget.isVisible());
            modifiedFields.add(UWAWidget.Field.VISIBLE);
        }
        if (widget.areParametersModified()) {
            uwaWidget.setParameters(stringify(widget.getParameters()));
            modifiedFields.add(UWAWidget.Field.PARAMETERS);
        }
    }

    private String stringify(Map<String, String> parameters) {
        JSONObject object = new JSONObject();
        for (Entry<String, String> entry : parameters.entrySet()) {
            try {
                object.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                // IGNORE;
            }
        }

        return object.toString();
    }

    /**
     * {@link Setter}
     * 
     * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
     */
    public class Setter implements AttributeHandler<UWAWidget> {

        private Widget widget;

        public Setter(Widget widget) {
            this.widget = widget;
        }

        public Object handle(Attribute<UWAWidget> attr, Object... args) {
            if (args[0] == null) {
                return null;
            }
            Field f = (Field) attr;
            switch (f) {
            case ID:
                widget.setId((String) args[0]);
                break;
            case TITLE:
                widget.setTitle((String) args[0]);
                break;
            case URL:
                widget.setURL((String) args[0]);
                break;
            case AUTOREFRESH:
                widget.setAutorefresh(Autoboxing.a2b(args[0]));
                break;
            case STANDALONE:
                widget.setStandalone(Autoboxing.a2b(args[0]));
                break;
            case VISIBLE:
                widget.setVisible(Autoboxing.a2b(args[0]));
                break;
            case PARAMETERS:
                setParameters((String) args[0]);
                break;
            default:
                return null;
            }
            return null;
        }

        private void setParameters(String params) {
            try {
                JSONObject object = new JSONObject(params);
                for (String key : object.keySet()) {
                    String value = object.get(key).toString();
                    widget.setParameter(key, value);
                }
            } catch (JSONException e) {
                LOG.error(e.getMessage(), e);
            }
        }

    }

}
