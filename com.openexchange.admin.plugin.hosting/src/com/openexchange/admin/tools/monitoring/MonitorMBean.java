
package com.openexchange.admin.tools.monitoring;

/**
 *
 * @author cutmasta
 */
public interface MonitorMBean {
    public long getNumberOfCreateResourceCalled();
    public long getNumberOfCreateContextCalled();
    public long getNumberOfCreateUserCalled();
    public long getNumberOfCreateGroupCalled();
}
