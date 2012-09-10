package com.openexchange.rss.preprocessors;

import java.io.UnsupportedEncodingException;

import org.joox.JOOX;
import org.joox.Match;

public class WhitelistPreprocessor extends AbstractPreprocessor {


	@Override
	public String process2(String payload) {
		try {
			replaceContent(payload);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return payload;
	}

	private void replaceContent(String payload) throws UnsupportedEncodingException {
		//byte[] data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><xml><myElement>My content</myElement><notMyElement>Not my content</notMyElement></xml>".getBytes("UTF-8");
		byte[] data = payload.getBytes("UTF-8");
		org.w3c.dom.Document document = JOOX.$(data).document();
		Match match = JOOX.$(document).find("myElement");
		System.out.println(match);
	}

}
