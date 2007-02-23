package com.openexchange.admin.daemons;

import com.openexchange.admin.AdminJobExecutorGlobalInterface;
import com.openexchange.admin.tools.AdminCacheExtended;

public class ClientAdminThreadExtended extends ClientAdminThread {
    public static AdminCacheExtended      cache       = null;
    public static AdminJobExecutorGlobalInterface ajx = null;
}
