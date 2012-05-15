
package com.openexchange.spamsettings.generic;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.osgi.SpamSettingsServiceRegistry;
import com.openexchange.spamsettings.generic.service.SpamSettingService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SpamSettingsParser}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SpamSettingsParser {

    public Map<String, Object> parse(final ServerSession session, final JSONObject json) throws JSONException, OXException {
        final FormContentParser formContentParser = new FormContentParser();
        final SpamSettingService service = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
        final DynamicFormDescription formDescription = service.getFormDescription(session);
        return formContentParser.parse(json, formDescription);
    }
}
