
package com.openexchange.admin.tools.monitoring;

/**
 *
 * @author cutmasta
 */
public class Monitor implements MonitorMBean{
    
    public long getNumberOfCreateResourceCalled() {
        return MonitoringInfos.getNumberOfCreateResourceCalled();
    }
    
    public long getNumberOfCreateContextCalled() {
        return MonitoringInfos.getNumberOfCreateContextCalled();
    }
               
    public long getNumberOfCreateUserCalled() {
        return MonitoringInfos.getNumberOfCreateUserCalled();
    }

    public long getNumberOfCreateGroupCalled() {
        return MonitoringInfos.getNumberOfCreateGroupCalled();
    }
    
}
