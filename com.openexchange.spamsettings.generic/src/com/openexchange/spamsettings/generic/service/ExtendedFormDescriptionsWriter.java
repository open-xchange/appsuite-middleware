
package com.openexchange.spamsettings.generic.service;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.datatypes.genericonf.json.FormDescriptionWriter;
import com.openexchange.datatypes.genericonf.json.ValueWriterSwitch;
import com.openexchange.i18n.Translator;


public class ExtendedFormDescriptionsWriter extends FormDescriptionWriter {

    private static final String OPTIONS = "options";

    private static final String MANDATORY = "mandatory";

    private static final String DEFAULT_VALUE = "defaultValue";

    private static final ValueWriterSwitch valueWrite = new ValueWriterSwitch();

    private final Translator translator;

    public ExtendedFormDescriptionsWriter(final Translator translator) {
        super(translator);
        this.translator = translator;
    }

    @Override
    public JSONArray write(final DynamicFormDescription form) throws JSONException {
        final JSONArray formDescriptionArray = new JSONArray();
        for (final FormElement formElement : form) {
            final JSONObject formElementObject;
            if (formElement instanceof ExtendedFormElement) {
                formElementObject = write((ExtendedFormElement)formElement);
            } else {
                formElementObject = write(formElement);
            }
            formDescriptionArray.put(formElementObject);
        }
        return formDescriptionArray;
    }

    public JSONObject write(final ExtendedFormElement formElement) throws JSONException {
        final JSONObject object = new JSONObject();
        if (formElement.getWidget() != Widget.CUSTOM) {
            object.put(WIDGET, formElement.getWidget().getKeyword());
        } else {
            object.put(WIDGET, formElement.getCustomWidget());
        }

        final Map<Integer, String> options = formElement.getOptionsNew();
        if(options != null && !options.isEmpty()) {
            final JSONObject jsonOptions = new JSONObject();

            final JSONArray keys = new JSONArray();
            final JSONArray values = new JSONArray();

            for (final Map.Entry<Integer, String> entry : options.entrySet())  {
                keys.put(entry.getKey().intValue());
                values.put(entry.getValue());
            }
            jsonOptions.putOpt("keys", keys);
            jsonOptions.put("values", values);
            object.put(OPTIONS, jsonOptions);
        }

        object.put(NAME, formElement.getName());
        object.put(DISPLAY_NAME, translator.translate(formElement.getDisplayName()));
        object.put(MANDATORY, formElement.isMandatory());
        if (null != formElement.getDefaultValue()) {
            object.put(DEFAULT_VALUE, formElement.getWidget().doSwitch(valueWrite, formElement.getDefaultValue()));
        }
        return object;
    }

}
