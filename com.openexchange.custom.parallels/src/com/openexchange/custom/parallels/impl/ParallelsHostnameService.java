

package com.openexchange.custom.parallels.impl;

import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.osgi.ParallelsServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;

/**
 * 
 * This service rewrites the hostname for the direct links which are sent via email to
 * appointments participants.
 * 
 * 
 * 
 * @author Manuel Kraft
 *
 */
public final class ParallelsHostnameService implements HostnameService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ParallelsHostnameService.class);

    @Override
    public String getHostname(final int userId, final int contextId) {
        if(contextId!=-1){
            final ContextService service = ParallelsServiceRegistry.getServiceRegistry().getService(ContextService.class);
            String hostname = null;
            Context ctx;
            try {
                ctx = service.getContext(contextId);
                final String[] login_mappings = ctx.getLoginInfo();
                final ConfigurationService configservice = ParallelsServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);

                // load suffix for branding string dynamically in loginmappings
                final String suffix_branded = configservice.getProperty("com.openexchange.custom.parallels.branding.suffix");
                // for debugging purposes
                if(LOG.isDebugEnabled()){
                    LOG.debug("Loaded loginmappings "+Arrays.toString(login_mappings)+" for context "+contextId);
                }
                boolean found_url = false;
                for (final String login_mapping : login_mappings) {
                    if(login_mapping.startsWith(suffix_branded)){
                        /**
                         * 
                         *  We found our mapping which contains the branded URL!
                         * 
                         *  Now split up the string to get the URL part
                         * 
                         */
                        final String[] URL_ = login_mapping.split("\\|\\|"); // perhaps replace with substring(start,end) if would be faster
                        if(URL_.length!=2){
                            LOG.fatal("Could not split up branded URL "+login_mapping+" login mapping for context "+contextId);
                        }else{
                            hostname = URL_[1];
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Successfully resolved HOST to "+hostname+" for branded context "+contextId);
                            }
                            found_url = true;
                        }
                    }
                }

                if(!found_url){
                    // now url was provisioned, load fall from configuration
                    hostname = configservice.getProperty("com.openexchange.custom.parallels.branding.fallbackurl");

                    LOG.debug("No branding URL was specified for context "+contextId+", using fallback from configuration file ");
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }

            return hostname;
        }else{
            LOG.fatal("Got context with id -1, dont generating any hostname");
            return null;
        }


    }

}
