package com.openexchange.realtime.group.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.GroupCommand;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.group.conversion.GroupCommand2JSON;
import com.openexchange.realtime.group.conversion.JSON2GroupCommand;
import com.openexchange.realtime.payload.converter.PayloadTreeConverter;
import com.openexchange.realtime.util.ElementPath;


public class RTGroupActivator extends HousekeepingActivator implements BundleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{MessageDispatcher.class, PayloadTreeConverter.class};
    }

    @Override
    protected void startBundle() throws Exception {
        GroupDispatcher.services = this;
    
        getService(PayloadTreeConverter.class).declarePreferredFormat(new ElementPath("group", "command"), GroupCommand.class.getName());
        
        registerService(SimplePayloadConverter.class, new GroupCommand2JSON());
        registerService(SimplePayloadConverter.class, new JSON2GroupCommand());
    
    }

}
