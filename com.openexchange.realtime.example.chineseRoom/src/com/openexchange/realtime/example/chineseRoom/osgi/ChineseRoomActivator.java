package com.openexchange.realtime.example.chineseRoom.osgi;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.example.chineseRoom.ChineseRoomComponent;
import com.openexchange.realtime.example.chineseRoom.JSON2LoggedMessage;
import com.openexchange.realtime.example.chineseRoom.LoggedMessage2JSON;

// The activator does the service wiring, as usual. 
public class ChineseRoomActivator extends HousekeepingActivator {
    
    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        // Register a component for creating a chat room in which
        // only pseudo-chinese can be spoken.
        registerService(Component.class, new ChineseRoomComponent(this));
    
        // Register converter that can convert a LoggedMessage into json ...
        registerService(SimplePayloadConverter.class, new LoggedMessage2JSON());
        
        // And back ... 
        registerService(SimplePayloadConverter.class, new JSON2LoggedMessage());
    
        // You have to provide these converters if you want to put non JSON classes into
        // Payload trees. 
        
        // Also, if you want to receive non-json data, which is usually very convenient, you
        // can declare that a payload with a certain namespace and element name should always be
        // Converted into a specific format. For that see the PayloadTreeConverter interface
        
        
    }

}
