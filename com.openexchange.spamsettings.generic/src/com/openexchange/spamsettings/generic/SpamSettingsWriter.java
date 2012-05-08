
package com.openexchange.spamsettings.generic;

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.i18n.Translator;
import com.openexchange.spamsettings.generic.osgi.SpamSettingsServiceRegistry;
import com.openexchange.spamsettings.generic.service.ExtendedFormDescriptionsWriter;
import com.openexchange.spamsettings.generic.service.SpamSettingException;
import com.openexchange.spamsettings.generic.service.SpamSettingService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SpamSettingsWriter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SpamSettingsWriter {

    public JSONArray write(final ServerSession session) throws JSONException, SpamSettingException {
        final FormDescriptionWriter formDescriptionWriter = new ExtendedFormDescriptionsWriter(Translator.EMPTY);
        final SpamSettingService service = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
        final DynamicFormDescription formDescription = service.getFormDescription(session);
        return formDescriptionWriter.write(formDescription);
    }
}
