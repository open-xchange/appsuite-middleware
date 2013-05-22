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

package com.openexchange.drive.json.json;

import java.util.Map;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.Action;


/**
 * {@link JsonDriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class JsonDriveAction<T extends DriveVersion> extends AbstractAction<T> {

    protected final Action action;

    public JsonDriveAction(Action action, T version, T newVersion, Map<String, Object> parameters) {
        super(version, newVersion);
        this.action = action;
        super.parameters.putAll(parameters);
    }

    @Override
    public Action getAction() {
        return action;
    }

    /**
     * Parses the supplied value into an enumeration constant, ignoring case.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param name The name of the constant to return
     * @return The enum constant
     * @throws IllegalArgumentException If there's no suitable enum constant for the supplied name
     */
    protected static <T extends Enum<T>> T parse(Class<T> enumeration, String name) {
        T value = parse(enumeration, name, null);
        if (null != value) {
            return value;
        }
        throw new IllegalArgumentException("No enum value '" + name + "' in Enum " + enumeration.getClass().getName());
    }

    /**
     * Parses the supplied value into an enumeration constant, ignoring case.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param name The name of the constant to return
     * @param defaultValue The enumeration constant to return if parsing fails
     * @return The enum constant
     */
    protected static <T extends Enum<T>> T parse(Class<T> enumeration, String name, T defaultValue) {
        for (T value : enumeration.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return defaultValue;
    }
}
