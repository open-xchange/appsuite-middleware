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

package com.openexchange.report.appsuite.serialization;

import java.io.Serializable;

public class ReportConfigs implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4288681340803505052L;

    private final String type;
    private final boolean isSingleDeployment;
    private final boolean isConfigTimerange;
    private final long consideredTimeframeStart;
    private final long consideredTimeframeEnd;

    //--------------------Constructors--------------------

    ReportConfigs(ReportConfigsBuilder builder) {
        super();
        this.type = builder.type;
        this.isSingleDeployment = builder.isSingleDeployment;
        this.isConfigTimerange = builder.isConfigTimerange;
        this.consideredTimeframeStart = builder.consideredTimeframeStart;
        this.consideredTimeframeEnd = builder.consideredTimeframeEnd;
    }

    //--------------------Getters and Setters--------------------

    public String getType() {
        return type;
    }

    public boolean isSingleDeployment() {
        return isSingleDeployment;
    }

    public long getConsideredTimeframeStart() {
        return consideredTimeframeStart;
    }

    public long getConsideredTimeframeEnd() {
        return consideredTimeframeEnd;
    }

    public boolean isConfigTimerange() {
        return isConfigTimerange;
    }

    //--------------------Builder--------------------

    /** A builder for an instance of <code>ReportConfigs</code> */
    public static class ReportConfigsBuilder {

        final String type;
        boolean isSingleDeployment;
        boolean isConfigTimerange;
        long consideredTimeframeStart;
        long consideredTimeframeEnd;

        public ReportConfigsBuilder(String type) {
            this.type = type;
        }

        public ReportConfigsBuilder isSingleDeployment(boolean isSingleDeployment) {
            this.isSingleDeployment = isSingleDeployment;
            return this;
        }

        public ReportConfigsBuilder isConfigTimerange(boolean isConfigTimerange) {
            this.isConfigTimerange = isConfigTimerange;
            return this;
        }

        public ReportConfigsBuilder consideredTimeframeStart(long consideredTimeframeStart) {
            this.consideredTimeframeStart = consideredTimeframeStart;
            return this;
        }

        public ReportConfigsBuilder consideredTimeframeEnd(long consideredTimeframeEnd) {
            this.consideredTimeframeEnd = consideredTimeframeEnd;
            return this;
        }

        public ReportConfigs build() {
            return new ReportConfigs(this);
        }
    }
}
