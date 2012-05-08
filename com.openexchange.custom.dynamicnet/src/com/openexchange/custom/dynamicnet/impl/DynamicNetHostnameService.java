
package com.openexchange.custom.dynamicnet.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.context.ContextService;
import com.openexchange.custom.dynamicnet.osgi.ContextRegisterer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;

/*
 * TODO: MAKE HOSTNAME PREFIX CONFIGURABLE!
 */
public final class DynamicNetHostnameService implements HostnameService {

    private static final Log LOG = LogFactory.getLog(DynamicNetHostnameService.class);

    @Override
    public String getHostname(final int userId, final int contextId) {
        final ContextService c = ContextRegisterer.getContextService();
        String hostname = null;
        String name = null;
        Context ctx;
        try {
            ctx = c.getContext(contextId);
            name = ctx.getName();
            final String bla[] = name.split("_");
            hostname = bla[1]+".ibone.ch/loginform_ox.php";
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }catch (final ArrayIndexOutOfBoundsException ax){
            // split failed, FATAL
            LOG.fatal("Error splitting context name \""+name+"\" (cid="+contextId+") by \"_\" for mail notification url!", ax);
        }
        return hostname;
    }

}
