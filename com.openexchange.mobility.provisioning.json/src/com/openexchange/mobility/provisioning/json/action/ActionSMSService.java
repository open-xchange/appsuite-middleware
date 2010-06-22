package com.openexchange.mobility.provisioning.json.action;

import com.openexchange.session.Session;


public interface ActionSMSService {
    
    public void sendMail(final Session session) throws ActionException;

}
