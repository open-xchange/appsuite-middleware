package com.openexchange.admin.console.util.server;

import com.openexchange.admin.console.util.UtilAbstraction;

public abstract class ServerAbstraction extends UtilAbstraction {

    @Override
    protected String getObjectName() {
        return "server";
    }
}
