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
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.frontend.uwa.UWAWidget;
import com.openexchange.frontend.uwa.UWAWidgetExceptionCodes;
import com.openexchange.frontend.uwa.UWAWidgetService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.modules.model.Attribute;


/**
 * {@link ContextOnlyUWAService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextOnlyUWAService implements UWAWidgetService {

    private final DatabaseService dbService;
    private final IDGeneratorService idGenerator;
    private final UserWidgetSQLStorage contextWidgets;
    private final int ctxId;

    public ContextOnlyUWAService(final DatabaseService dbService, final IDGeneratorService idGenerator, final int ctxId) {
        this.dbService = dbService;
        this.idGenerator = idGenerator;
        this.contextWidgets = new UserWidgetSQLStorage(UWAWidget.METADATA, dbService, 0, ctxId);
        this.ctxId = ctxId;
    }

    @Override
    public List<UWAWidget> all() throws OXException {
        try {
            return contextWidgets.load();
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

    @Override
    public void create(final UWAWidget widget) throws OXException {
        UWAUtility.checkUrl(widget);
        try {
            final int dbId = idGenerator.getId("uwaWidget", ctxId);
            widget.setId(String.valueOf(dbId));
            contextWidgets.create(widget);
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

    @Override
    public void delete(final String id) throws OXException {
        try {
            contextWidgets.delete(id);
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

    @Override
    public UWAWidget get(final String id) throws OXException {
        try {
            return contextWidgets.load(id);
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

    @Override
    public void update(final UWAWidget widget, final List<? extends Attribute<UWAWidget>> modified) throws OXException {
        UWAUtility.checkUrl(widget);
        try {
            contextWidgets.update(widget, modified);
        } catch (final SQLException x) {
            throw UWAWidgetExceptionCodes.SQLError.create(x.getMessage());
        }
    }

}
