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

package com.openexchange.gdpr.dataexport;

import java.util.Optional;
import org.json.JSONObject;

/**
 * {@link PauseResult} - Represents the result when calling {@link DataExportProvider#pause(java.util.UUID, DataExportSink, DataExportTask) pause()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class PauseResult {

    private static final PauseResult UNPAUSED = new PauseResult(false, null);
    private static final PauseResult PAUSED = new PauseResult(true, Optional.empty());

    /**
     * Gets the unpaused result.
     *
     * @return The unpaused result
     */
    public static PauseResult unpaused() {
        return UNPAUSED;
    }

    /**
     * Gets the paused result.
     *
     * @param optionalSavePoint The optional save-point
     * @return The paused result
     */
    public static PauseResult paused(Optional<JSONObject> optionalSavePoint) {
        return optionalSavePoint.isPresent() ? new PauseResult(true, optionalSavePoint) : PAUSED;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final boolean paused;
    private final Optional<JSONObject> jSavePoint;

    private PauseResult(boolean paused, Optional<JSONObject> jSavePoint) {
        super();
        this.paused = paused;
        this.jSavePoint = jSavePoint;
    }

    /**
     * Checks if provider managed to immediately pause processing.
     * <p>
     * In case of <code>false</code>, appropriate result is returned through
     * {@link DataExportProvider#export(java.util.UUID, DataExportSink, Optional, DataExportTask) export()}.
     *
     * @return <code>true</code> if paused; otherwise <code>false</code> if still running (but pausing has been requested)
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Gets the optional save-point
     * <p>
     * Only available if this result signals {@link #isPaused() paused}.
     *
     * @return The optional save-point
     */
    public Optional<JSONObject> getSavePoint() {
        return jSavePoint;
    }

}
