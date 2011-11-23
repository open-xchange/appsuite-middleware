package com.openexchange.test.fixtures.ajax;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.TestUserConfig;

public class AJAXUserConfig implements TestUserConfig {

	private final AJAXClient client;

	public AJAXUserConfig(AJAXClient client) {
		this.client = client;
	}

	@Override
    public Object get(Tree tree) {
		try {
			return ConfigTools.get(client,
			        new GetRequest(tree)).getData();
		} catch (OXException e) {
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

	@Override
    public boolean getBool(Tree tree) {
		return (Boolean) get(tree);
	}

	@Override
    public int getInt(Tree tree) {
		return (Integer) get(tree);
	}

	@Override
    public long getLong(Tree tree) {
		return (Long) get(tree);
	}

	@Override
    public String getString(Tree tree) {
		return get(tree).toString();
	}

	@Override
    public void set(Tree tree, Object value) {
		try {
			ConfigTools.set(client, new SetRequest(tree, value));
		} catch (OXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
