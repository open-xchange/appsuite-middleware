package com.openexchange.realtime.example.chineseroom.json.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.example.chineseroom.json.ChineseActions;

public class ChineseJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{MessageDispatcher.class};
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new ChineseActions(this), "china/room");
    }




}
