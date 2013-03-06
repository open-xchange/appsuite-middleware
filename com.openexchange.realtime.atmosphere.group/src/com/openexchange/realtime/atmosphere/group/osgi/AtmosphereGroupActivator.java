package com.openexchange.realtime.atmosphere.group.osgi;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.payload.converter.AtmospherePayloadElementConverter;
import com.openexchange.realtime.group.GroupCommand;
import com.openexchange.realtime.util.ElementPath;


public class AtmosphereGroupActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(SimplePayloadConverter.class, new GroupCommand2JSON());
        registerService(SimplePayloadConverter.class, new JSON2GroupCommand());
        
        registerService(AtmospherePayloadElementConverter.class, new AtmospherePayloadElementConverter(GroupCommand.class.getSimpleName(), new ElementPath("group", "command")));
    }

}
