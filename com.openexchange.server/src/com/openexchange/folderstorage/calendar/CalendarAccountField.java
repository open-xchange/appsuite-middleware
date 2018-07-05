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

package com.openexchange.folderstorage.calendar;

import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;

/**
 * {@link CalendarAccountField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAccountField extends FolderField {

    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3202;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.calendar.account";

    private static final long serialVersionUID = -8172813977801568827L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarAccountField.class);
    private static final CalendarAccountField INSTANCE = new CalendarAccountField();

    /**
     * Gets the extended properties field instance.
     *
     * @return The instance
     */
    public static CalendarAccountField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CalendarAccountField}.
     */
    private CalendarAccountField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

    @Override
    public FolderProperty parse(Object value) {
        if (null != value) {
            try {
                return new FolderProperty(getName(), readAccount((JSONObject) value));
            } catch (Exception e) {
                LOG.warn("Error parsing extended calendar properties from \"{}\": {}", value, e.getMessage(), e);
            }
        }
        return new FolderProperty(getName(), null);
    }

    @Override
    public Object write(FolderProperty property) {
        if (null != property) {
            try {
                return writeAccount((CalendarAccount) property.getValue());
            } catch (Exception e) {
                LOG.warn("Error writing calendar account \"{}\": {}", property.getValue(), e.getMessage(), e);
            }
        }
        return getDefaultValue();
    }

    private static CalendarAccount readAccount(JSONObject jsonObject) throws JSONException {
        return new DefaultCalendarAccount(
            jsonObject.optString("provider", null),
            jsonObject.optInt("id"),
            0,
            null,
            jsonObject.optJSONObject("config"),
            jsonObject.has("timestamp") ? new Date(jsonObject.getLong("timestamp")) : null
        );
    }

    private static JSONObject writeAccount(CalendarAccount account) throws JSONException {
        return new JSONObject()
            .put("id", account.getAccountId())
            .put("provider", account.getProviderId())
            .putOpt("timestamp", null != account.getLastModified() ? L(account.getLastModified().getTime()) : null)
            .putOpt("config", account.getUserConfiguration())
        ;
    }

}
