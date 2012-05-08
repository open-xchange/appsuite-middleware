
package com.openexchange.spamsettings.generic.service;

import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SpamSettingService}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface SpamSettingService {

    /**
     * Provides the Form Description to be displayed in the Configuration part of the User Interface.
     * 
     * @param session a Session
     * @return The Form Description
     */
    public DynamicFormDescription getFormDescription(ServerSession session) throws SpamSettingException;

    /**
     * The current settings of the Spam Configuration.
     * 
     * @param session a Session
     * @return The setting pairs
     */
    public Map<String, Object> getSettings(ServerSession session) throws SpamSettingException;

    /**
     * Writes the settings of the Spam Configuration.
     * 
     * @param session a Session
     * @param settings The setting pairs to be written
     */
    public void writeSettings(ServerSession session, Map<String, Object> settings) throws SpamSettingException;

}
