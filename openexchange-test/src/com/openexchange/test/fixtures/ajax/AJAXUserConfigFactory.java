package com.openexchange.test.fixtures.ajax;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.test.fixtures.SimpleCredentials;
import com.openexchange.test.fixtures.TestUserConfig;
import com.openexchange.test.fixtures.TestUserConfigFactory;
import com.openexchange.tools.servlet.AjaxException;

public class AJAXUserConfigFactory implements TestUserConfigFactory {

	public TestUserConfig create(SimpleCredentials credentials) {
		AJAXSession session = new AJAXSession();
		
        try {
			session.setId(LoginTools.login(
					session, new LoginRequest(credentials.getLogin(), credentials.getPassword())).getSessionId());
			AJAXClient client = new AJAXClient(session);
			return new AJAXUserConfig(client);
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
