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

package com.openexchange.gdpr.dataexport.provider.general;

import java.util.Optional;
import org.json.JSONObject;
import com.openexchange.gdpr.dataexport.ExportResult;

/**
 * {@link SavePointAndReason} - Wraps a save-point and an optional reason.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SavePointAndReason {

    /**
     * Initializes a new {@link SavePointAndReason}.
     *
     * @param savePoint The save-point
     * @return The new save-point
     */
    public static SavePointAndReason savePointFor(JSONObject savePoint) {
        return savePointFor(savePoint, null);
    }

    /**
     * Initializes a new {@link SavePointAndReason}.
     *
     * @param savePoint The save-point
     * @param reason The reason or <code>null</code>
     * @return The new save-point
     */
    public static SavePointAndReason savePointFor(JSONObject savePoint, Exception reason) {
        return new SavePointAndReason(savePoint, reason);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final JSONObject savePoint;
    private final Optional<Exception> reason;

    /**
     * Initializes a new {@link SavePointAndReason}.
     *
     * @param savePoint The save-point
     * @param reason The reason or <code>null</code>
     */
    private SavePointAndReason(JSONObject savePoint, Exception reason) {
        super();
        this.savePoint = savePoint;
        this.reason = Optional.ofNullable(reason);
    }

    /**
     * Gets the savePoint
     *
     * @return The savePoint
     */
    public JSONObject getSavePoint() {
        return savePoint;
    }

    /**
     * Gets the optional reason
     *
     * @return The optional reason
     */
    public Optional<Exception> getReason() {
        return reason;
    }

    /**
     * Gets the <i>incomplete</i> export result for this wrapper.
     *
     * @return The export result
     */
    public ExportResult result() {
        return ExportResult.incomplete(Optional.of(savePoint), reason);
    }

}
