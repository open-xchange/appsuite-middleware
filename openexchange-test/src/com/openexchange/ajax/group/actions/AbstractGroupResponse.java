
package com.openexchange.ajax.group.actions;

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.GroupParser;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;

public abstract class AbstractGroupResponse extends AbstractAJAXResponse {

    private Group[] groups;

    public AbstractGroupResponse(Response response) {
        super(response);
    }

    public Group[] getGroups() throws OXException, JSONException {
        if (null == groups) {
            final JSONArray json = (JSONArray) getData();
            groups = new Group[json.length()];
            final GroupParser parser = new GroupParser();
            for (int i = 0; i < json.length(); i++) {
                groups[i] = new Group();
                parser.parse(groups[i], json.getJSONObject(i));
            }
        }
        return groups;
    }

}
