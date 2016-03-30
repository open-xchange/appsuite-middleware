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

package com.openexchange.frontend.uwa.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.frontend.uwa.UWAWidget;
import com.openexchange.frontend.uwa.UWAWidget.Field;
import com.openexchange.frontend.uwa.UWAWidgetExceptionCodes;
import com.openexchange.frontend.uwa.UWAWidgetService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Tools;
import com.openexchange.modules.storage.memory.MemoryStorage;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link CompositeUWAService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeUWAService implements UWAWidgetService {

    private UserWidgetSQLStorage userScope = null;

    private UserWidgetSQLStorage contextScope = null;

    private MemoryStorage<UWAWidget> serverScope = null;

    private PositionSQLStorage positions = null;

    private final IDGeneratorService idGenerator;

    private final int ctxId;

    public CompositeUWAService(final DatabaseService dbService, final ConfigViewFactory configViews, final ConfigurationService config, final IDGeneratorService idGenerator, final int userId, final int ctxId) throws OXException, OXException {
        userScope = new UserWidgetSQLStorage(UWAWidget.METADATA, dbService, userId, ctxId);
        contextScope = new UserWidgetSQLStorage(UWAWidget.METADATA, dbService, 0, ctxId);
        positions = new PositionSQLStorage(UWAWidget.METADATA, dbService, userId, ctxId);

        final ConfigView view = configViews.getView(userId, ctxId);
        final String filename = view.get("com.openexchange.frontend.uwa.widgetFile", String.class);
        Map<String, Map<String, Object>> configValues = null;
        if (filename != null) {
            configValues = ensureType(config.getYaml(filename));
        } else {
            configValues = Collections.emptyMap();
        }

        serverScope = new MemoryStorage<UWAWidget>(configValues, UWAWidget.METADATA);

        this.idGenerator = idGenerator;
        this.ctxId = ctxId;
    }


    private Map<String, Map<String, Object>> ensureType(final Object yaml) throws OXException {
        if (Map.class.isInstance(yaml)) {
            for(final Map.Entry<Object, Object> entry : ((Map<Object, Object>) yaml).entrySet()) {
                final Object key = entry.getKey();
                final Object value = entry.getValue();
                if(!Map.class.isInstance(value)) {
                    throw UWAWidgetExceptionCodes.INVALID_CONFIGURATION.create();
                }
            }
        } else {
            throw UWAWidgetExceptionCodes.INVALID_CONFIGURATION.create();
        }
        return (Map<String, Map<String, Object>>) yaml;
    }


    private static final Set<Field> POSITION_FIELDS = EnumSet.of(Field.ADJ);

    @Override
    public List<UWAWidget> all() throws OXException {
        try {
            final List<UWAWidget> userWidgets = userScope.load();
            Tools.set(userWidgets, UWAWidget.Field.PROTECTED, false);

            final List<UWAWidget> contextWidgets = contextScope.load();
            Tools.set(contextWidgets, UWAWidget.Field.PROTECTED, true);

            final List<UWAWidget> serverWidgets = serverScope.list();
            Tools.set(serverWidgets, UWAWidget.Field.PROTECTED, true);

            final List<UWAWidget> positionInformation = positions.load();

            final List<UWAWidget> all = new ArrayList<UWAWidget>(userWidgets.size() + contextWidgets.size() + serverWidgets.size());

            all.addAll(scope("user", userWidgets));
            all.addAll(scope("context", contextWidgets));
            all.addAll(scope("server", serverWidgets));

            final Map<String, UWAWidget> positionMap = new HashMap<String, UWAWidget>();
            for (final UWAWidget widget : positionInformation) {
                positionMap.put(widget.getId(), widget);
            }

            for (final UWAWidget widget : all) {
                final UWAWidget position = positionMap.get(widget.getId());
                if(position != null) {
                    for (final Field field : POSITION_FIELDS) {
                        widget.set(field, position.get(field));
                    }
                }
            }

            return all;

        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

    private List<UWAWidget> scope(final String scope, final List<UWAWidget> widgets) {
        for (final UWAWidget widget : widgets) {
            scope(scope, widget);
        }
        return widgets;
    }

    private void scope(final String scope, final UWAWidget widget) {
        widget.setId(IDMangler.mangle(scope, widget.getId()));
    }

    @Override
    public void create(final UWAWidget widget) throws OXException {
        UWAUtility.checkUrl(widget);
        try {
            final int dbId = idGenerator.getId("uwaWidget", ctxId);
            final String id = IDMangler.mangle("user", ""+dbId);

            widget.setId(String.valueOf(dbId));
            userScope.create(widget);

            widget.setId(id);
            positions.create(widget);

        } catch (final SQLException e) {
            throw UWAWidgetExceptionCodes.SQLError.create(e.getMessage());
        }

    }

    @Override
    public void delete(final String id) throws OXException {
        if (isProtected(id)) {
            throw UWAWidgetExceptionCodes.PROTECTED.create(id);
        }

        try {
            final List<String> components = IDMangler.unmangle(id);

            userScope.delete(components.get(1));
            positions.delete(id);
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }

    }

    @Override
    public UWAWidget get(final String id) throws OXException {
        final List<String> components = IDMangler.unmangle(id);
        final String scope = components.get(0);
        final String unscopedId = components.get(1);

        UWAWidget widget = null;
        if (scope.equals("server")) {
            widget = serverScope.get(unscopedId);
        } else {
            try {
                widget = userScope.load(unscopedId);
            } catch (final SQLException x) {
                throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
            }
            if (widget == null) {
                throw UWAWidgetExceptionCodes.NOT_FOUND.create(id);
            }
        }

        widget.setId(id);

        try {
            final UWAWidget position = positions.load(id);
            if (position != null) {
                for (final Field field : POSITION_FIELDS) {
                    widget.set(field, position.get(field));
                }
            }
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }

        return widget;
    }

    @Override
    public void update(final UWAWidget widget, final List<? extends Attribute<UWAWidget>> modified) throws OXException {
        UWAUtility.checkUrl(widget);
        if (isProtected(widget.getId())) {
            if (!onlyPositionFields(modified)) {
                throw UWAWidgetExceptionCodes.PROTECTED.create(widget.getId());
            }
            positionUpdate(widget, modified);
        } else {
            regularUpdate(widget, modified);
        }
    }

    private void regularUpdate(final UWAWidget widget, final List<? extends Attribute<UWAWidget>> modified) throws OXException {
        final String id = widget.getId();
        final String dbId = IDMangler.unmangle(id).get(1);
        try {
            widget.setId(dbId);
            userScope.update(widget, modified);
        } catch (final SQLException e) {
            throw UWAWidgetExceptionCodes.SQLError.create(e.getMessage());
        }
        widget.setId(id);
        positionUpdate(widget, modified);
    }

    private void positionUpdate(final UWAWidget widget, final List<? extends Attribute<UWAWidget>> modified) throws OXException {
        try {
            if (positions.exists(widget.getId())) {
                positions.update(widget, modified);
            } else {
                positions.create(widget);
            }
        } catch (final SQLException e) {
            throw UWAWidgetExceptionCodes.SQLError.create(e.getMessage());
        }
    }

    private boolean onlyPositionFields(final List<? extends Attribute<UWAWidget>> modified) {
        final Set<Attribute<UWAWidget>> fieldSet = new HashSet<Attribute<UWAWidget>>(modified);
        fieldSet.removeAll(POSITION_FIELDS);
        fieldSet.remove(Field.ID);
        return fieldSet.isEmpty();
    }

    private boolean isProtected(final String scopedId) throws OXException {
        final List<String> components = IDMangler.unmangle(scopedId);
        final String scope = components.get(0);
        try {
            return !(scope.equals("user") && userScope.isUserWidget(components.get(1)));
        } catch (final SQLException e) {
            throw UWAWidgetExceptionCodes.SQLError.create(e.getMessage());
        }
    }

}
