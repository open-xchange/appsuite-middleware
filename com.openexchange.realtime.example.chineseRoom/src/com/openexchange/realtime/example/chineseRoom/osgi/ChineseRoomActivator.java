package com.openexchange.realtime.example.chineseRoom.osgi;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.example.chineseRoom.ChineseRoomComponent;
import com.openexchange.realtime.example.chineseRoom.JSON2LoggedMessage;
import com.openexchange.realtime.example.chineseRoom.LoggedMessage2JSON;


public class ChineseRoomActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(Component.class, new ChineseRoomComponent(this));
        registerService(SimplePayloadConverter.class, new LoggedMessage2JSON());
        registerService(SimplePayloadConverter.class, new JSON2LoggedMessage());
    }

}
