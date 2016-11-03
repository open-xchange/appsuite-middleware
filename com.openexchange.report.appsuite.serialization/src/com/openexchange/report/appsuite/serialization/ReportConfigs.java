
package com.openexchange.report.appsuite.serialization;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportConfigs implements Serializable, CompositeData {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4288681340803505052L;
    private static final Logger LOG = LoggerFactory.getLogger(ReportConfigs.class);

    private HashMap<String, Object> attributeMap;

    @Override
    public CompositeType getCompositeType() {
        CompositeType compType = null;
        try {
            compType = new CompositeType("ReportConfigs", "ReportConfigs", new String[] {"type", "isSingleDeployment", "consideredTimeframeStart", "consideredTimeframeEnd", "isConfigTimerange"}
                                                                         , new String[] {"type", "isSingleDeployment", "consideredTimeframeStart", "consideredTimeframeEnd", "isConfigTimerange"}
                                                                         , new OpenType[] {SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.BOOLEAN});
        } catch (OpenDataException e) {
            LOG.error("Unable to create CompositeType of report", e);
        }
        return compType;
    }

    @Override
    public Object get(String key) {
        return this.attributeMap.get(key);
    }

    @Override
    public Object[] getAll(String[] keys) {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return this.attributeMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Collection<?> values() {
        return (Collection<?>) this.attributeMap;
    }
    //--------------------Constructors--------------------

    public static ReportConfigs from(CompositeData cd) {
        return new ReportConfigsBuilder((String) cd.get("type"))
            .isSingleDeployment((boolean) cd.get("isSingleDeployment"))
            .isConfigTimerange((boolean) cd.get("isConfigTimerange"))
            .consideredTimeframeStart((long) cd.get("consideredTimeframeStart"))
            .consideredTimeframeEnd((long) cd.get("consideredTimeframeEnd"))
            .build();
    }

    private ReportConfigs(ReportConfigsBuilder builder) {
        super();
        this.attributeMap = new HashMap<>();
        this.attributeMap.put("type", builder.type);
        this.attributeMap.put("isSingleDeployment", builder.isSingleDeployment);
        this.attributeMap.put("isConfigTimerange", builder.isConfigTimerange);
        this.attributeMap.put("consideredTimeframeStart", builder.consideredTimeframeStart);
        this.attributeMap.put("consideredTimeframeEnd", builder.consideredTimeframeEnd);
    }

    private ReportConfigs(String type, boolean isSingleDeployment, boolean isConfigTimerange, long consideredTimeframeStart, long consideredTimeframeEnd) {
        super();
        this.attributeMap = new HashMap<>();
        this.attributeMap.put("type", type);
        this.attributeMap.put("isSingleDeployment", isSingleDeployment);
        this.attributeMap.put("isConfigTimerange", isConfigTimerange);
        this.attributeMap.put("consideredTimeframeStart", consideredTimeframeStart);
        this.attributeMap.put("consideredTimeframeEnd", consideredTimeframeEnd);
    }

    //--------------------Getters and Setters--------------------

    public String getType() {
        return (String) this.attributeMap.get("type");
    }

    public boolean isSingleDeployment() {
        return (boolean) this.attributeMap.get("isSingleDeployment");
    }

    public long getConsideredTimeframeStart() {
        return (long) this.attributeMap.get("consideredTimeframeStart");
    }

    public long getConsideredTimeframeEnd() {
        return (long) this.attributeMap.get("consideredTimeframeEnd");
    }

    public boolean isConfigTimerange() {
        return (boolean) this.attributeMap.get("isConfigTimerange");
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

        public ReportConfigs build() {
            return new ReportConfigs(this);
        }
    }
}
