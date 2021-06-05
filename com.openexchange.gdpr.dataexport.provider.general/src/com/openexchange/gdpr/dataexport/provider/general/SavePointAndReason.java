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
