package com.openexchange.test.fixtures.ajax;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.group.actions.GetRequest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.fixtures.GroupResolver;
import com.openexchange.tools.servlet.AjaxException;

public class AJAXGroupResolver implements GroupResolver {
	private AJAXClient client;
	private AJAXContactFinder contactFinder;
	
	public AJAXGroupResolver(AJAXClient client) {
		super();
		this.client = client;
		this.contactFinder = new AJAXContactFinder(client);
	}

	public Contact[] resolveGroup(String simpleName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Contact[] resolveGroup(int groupId) {
		GetRequest group = new GetRequest(groupId);
		try {
			AbstractAJAXResponse response = client.execute(group);
			JSONObject data = (JSONObject) response.getData();
			JSONArray members = data.getJSONArray("members");
			Contact[] groupMembers = new Contact[members.length()];
			for(int i = 0; i < groupMembers.length; i++){
				int userId = members.getInt(i);
				groupMembers[i] = contactFinder.getContact(userId);
			}
			return groupMembers;
		} catch (AjaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

}
