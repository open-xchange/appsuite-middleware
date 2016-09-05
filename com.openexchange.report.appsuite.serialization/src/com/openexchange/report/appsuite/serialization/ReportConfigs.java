
package com.openexchange.report.appsuite.serialization;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import com.openexchange.config.ConfigurationService;
import com.openexchange.report.appsuite.serialization.internal.Services;
import com.openexchange.report.appsuite.serialization.osgi.StringParserServiceRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.strings.StringParser;

public class ReportConfigs implements Serializable, CompositeData{

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4288681340803505052L;

    private String type;

    private boolean isSingleDeployment = true;

    private Long consideredTimeframeStart;

    private Long consideredTimeframeEnd;

    private boolean isConfigTimerange;
    
    //--------------------OXCS-Report, relevant attributes--------------------

    private boolean isShowSingleTenant;

    private Long singleTenantId;

    private boolean isAdminIgnore;

    private boolean isShowDriveMetrics;

    private boolean isShowMailMetrics;
    
    private HashMap<String, Object> attributeMap;

    @Override
    public CompositeType getCompositeType() {
        CompositeType compType = null;
        try {
            compType = new CompositeType("ReportConfigs", "ReportConfigs", new String[] {"type", "isSingleDeployment", "consideredTimeframeStart", "consideredTimeframeEnd", "isConfigTimerange", "isShowSingleTenant", "singleTenantId", "isAdminIgnore", "isShowDriveMetrics", "isShowMailMetrics"}
                                                                         , new String[] {"type", "isSingleDeployment", "consideredTimeframeStart", "consideredTimeframeEnd", "isConfigTimerange", "isShowSingleTenant", "singleTenantId", "isAdminIgnore", "isShowDriveMetrics", "isShowMailMetrics"}
                                                                         , new OpenType[] {SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN});
        } catch (OpenDataException e) {
            e.printStackTrace();
        }
        return compType;
    }

    @Override
    public Object get(String key) {
        return this.attributeMap.get(key);
    }

    @Override
    public Object[] getAll(String[] keys) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return this.attributeMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<?> values() {
        return (Collection<?>) this.attributeMap;
    }
    //--------------------Constructors--------------------
    
    public static ReportConfigs from(CompositeData cd) {
        return new ReportConfigs((String) cd.get("type"), 
                                (boolean) cd.get("isSingleDeployment"), 
                                (boolean) cd.get("isConfigTimerange"), 
                                (Long) cd.get("consideredTimeframeStart"), 
                                (Long) cd.get("consideredTimeframeEnd"), 
                                (boolean) cd.get("isShowSingleTenant"), 
                                (Long) cd.get("singleTenantId"), 
                                (boolean) cd.get("isAdminIgnore"), 
                                (boolean) cd.get("isShowDriveMetrics"), 
                                (boolean) cd.get("isShowMailMetrics"));
    }

    public ReportConfigs(String type, boolean isSingleDeployment, boolean isConfigTimerange, Long consideredTimeframeStart, Long consideredTimeframeEnd, boolean isShowSingleTenant, Long singleTenantId, boolean isAdminIgnore, boolean isShowDriveMetrics, boolean isShowMailMetrics) {
        super();
        
        this.type = type;
        this.isSingleDeployment = isSingleDeployment;
        this.isConfigTimerange = isConfigTimerange;
        this.consideredTimeframeStart = consideredTimeframeStart;
        this.consideredTimeframeEnd = consideredTimeframeEnd;
        this.isShowSingleTenant = isShowSingleTenant;
        this.singleTenantId = singleTenantId;
        this.isAdminIgnore = isAdminIgnore;
        this.isShowDriveMetrics = isShowDriveMetrics;
        this.isShowMailMetrics = isShowMailMetrics;
        this.attributeMap = new HashMap<>();
        this.attributeMap.put("type", type);
        this.attributeMap.put("isSingleDeployment", isSingleDeployment);
        this.attributeMap.put("isConfigTimerange", isConfigTimerange);
        this.attributeMap.put("consideredTimeframeStart", consideredTimeframeStart);
        this.attributeMap.put("consideredTimeframeEnd", consideredTimeframeEnd);
        this.attributeMap.put("isShowSingleTenant", isShowSingleTenant);
        this.attributeMap.put("singleTenantId", singleTenantId);
        this.attributeMap.put("isAdminIgnore", isAdminIgnore);
        this.attributeMap.put("isShowDriveMetrics", isShowDriveMetrics);
        this.attributeMap.put("isShowMailMetrics", isShowMailMetrics);
    }

    //--------------------Getters and Setters--------------------

    public String getType() {
        if (this.type == null)
           this.type = "default"; 
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSingleDeployment() {
        return isSingleDeployment;
    }

    public void setSingleDeployment(boolean isSingleDeployment) {
        this.isSingleDeployment = isSingleDeployment;
    }

    public Long getConsideredTimeframeStart() {
        return consideredTimeframeStart;
    }

    public void setConsideredTimeframeStart(Long consideredTimeframeStart) {
        this.consideredTimeframeStart = consideredTimeframeStart;
    }

    public Long getConsideredTimeframeEnd() {
        return consideredTimeframeEnd;
    }

    public void setConsideredTimeframeEnd(Long consideredTimeframeEnd) {
        this.consideredTimeframeEnd = consideredTimeframeEnd;
    }

    public boolean isShowSingleTenant() {
        return isShowSingleTenant;
    }

    public void setShowSingleTenant(boolean isShowSingleTenant) {
        this.isShowSingleTenant = isShowSingleTenant;
    }

    public Long getSingleTenantId() {
        return singleTenantId;
    }

    public void setSingleTenantId(Long singleTenantId) {
        this.singleTenantId = singleTenantId;
    }

    public boolean isAdminIgnore() {
        return isAdminIgnore;
    }

    public void setAdminIgnore(boolean isAdminIgnore) {
        this.isAdminIgnore = isAdminIgnore;
    }

    public boolean isShowDriveMetrics() {
        return isShowDriveMetrics;
    }

    public void setShowDriveMetrics(boolean isShowDriveMetrics) {
        this.isShowDriveMetrics = isShowDriveMetrics;
    }

    public boolean isShowMailMetrics() {
        return isShowMailMetrics;
    }

    public void setShowMailMetrics(boolean isShowMailMetrics) {
        this.isShowMailMetrics = isShowMailMetrics;
    }

    public boolean isConfigTimerange() {
        return isConfigTimerange;
    }

    public void setConfigTimerange(boolean isConfigTimerange) {
        this.isConfigTimerange = isConfigTimerange;
    }
}
