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

package com.openexchange.report.client.configuration;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.l;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Report configurations.
 * 
 * {@link ReportConfigs}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.3
 */
public class ReportConfigs implements Serializable {

    private static final String IS_CONFIG_TIMERANGE = "isConfigTimerange";
    private static final String CONSIDERED_TIMEFRAME_END = "consideredTimeframeEnd";
    private static final String CONSIDERED_TIMEFRAME_START = "consideredTimeframeStart";
    private static final String IS_SINGLE_DEPLOYMENT = "isSingleDeployment";
    private static final String TYPE = "type";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4288681340803505896L;

    private HashMap<String, Object> attributeMap;

    @SuppressWarnings("synthetic-access")
    private ReportConfigs(ReportConfigsBuilder builder) {
        super();
        this.attributeMap = new HashMap<>();
        this.attributeMap.put(TYPE, builder.type);
        this.attributeMap.put(IS_SINGLE_DEPLOYMENT, B(builder.isSingleDeployment));
        this.attributeMap.put(IS_CONFIG_TIMERANGE, B(builder.isConfigTimerange));
        this.attributeMap.put(CONSIDERED_TIMEFRAME_START, L(builder.consideredTimeframeStart));
        this.attributeMap.put(CONSIDERED_TIMEFRAME_END, L(builder.consideredTimeframeEnd));
    }

    //--------------------Getters and Setters--------------------

    public String getType() {
        return (String) this.attributeMap.get(TYPE);
    }

    public boolean isSingleDeployment() {
        return b(Boolean.class.cast(this.attributeMap.get(IS_SINGLE_DEPLOYMENT)));
    }

    public long getConsideredTimeframeStart() {
        return l(Long.class.cast(this.attributeMap.get(CONSIDERED_TIMEFRAME_START)));
    }

    public long getConsideredTimeframeEnd() {
        return l(Long.class.cast(this.attributeMap.get(CONSIDERED_TIMEFRAME_END)));
    }

    public boolean isConfigTimerange() {
        return b(Boolean.class.cast(this.attributeMap.get(IS_CONFIG_TIMERANGE)));
    }

    public static class ReportConfigsBuilder {

        private String type;
        private boolean isSingleDeployment;
        private boolean isConfigTimerange;
        private long consideredTimeframeStart;
        private long consideredTimeframeEnd;

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

        @SuppressWarnings("synthetic-access")
        public ReportConfigs build() {
            return new ReportConfigs(this);
        }
    }
}
