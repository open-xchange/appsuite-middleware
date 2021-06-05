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

package com.openexchange.health.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.MWHealthState;


/**
 * {@link MWHealthCheckResultImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class MWHealthCheckResultImpl implements MWHealthCheckResult {

    private final MWHealthState overallState;
    private final List<MWHealthCheckResponse> responses;
    private final List<String> ignoredChecks;
    private final List<String> skippedChecks;

    public MWHealthCheckResultImpl(MWHealthState overallState, List<MWHealthCheckResponse> responses, List<String> ignoredChecks, List<String> skippedChecks) {
        super();
        this.overallState = overallState;
        this.responses = responses;
        this.ignoredChecks = ignoredChecks;
        this.skippedChecks = skippedChecks;
    }
    @Override
    public MWHealthState getStatus() {
        return overallState;
    }

    @Override
    public List<MWHealthCheckResponse> getChecks() {
        return Collections.unmodifiableList(responses);
    }

    @Override
    public List<String> getSkippedChecks() {
        return Collections.unmodifiableList(skippedChecks);
    }

    @Override
    public List<String> getIgnoredResponses() {
        return Collections.unmodifiableList(ignoredChecks);
    }

}
