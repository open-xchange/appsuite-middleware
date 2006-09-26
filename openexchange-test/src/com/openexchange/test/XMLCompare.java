package com.openexchange.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLCompare {

		
	private static Map<String,Method>  methods = new HashMap<String,Method>();
	private static boolean inited;
	private Set<String> checkTextNames = Collections.EMPTY_SET;
	
	public boolean compare(String expect, String got) throws UnsupportedEncodingException, JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document expectedDoc = builder.build(new ByteArrayInputStream(expect.getBytes("UTF-8")));
		Document gotDoc = builder.build(new ByteArrayInputStream(got.getBytes("UTF-8")));
		
		return compareDocuments(expectedDoc.getRootElement(), gotDoc.getRootElement());
	}
	
	public boolean compareDocuments(Element expectedDoc, Element gotDoc) {
		//System.out.println(expectedDoc.getName()+" == "+gotDoc.getName()+" ? "+expectedDoc.getName().equals(gotDoc.getName()));
		if(!expectedDoc.getName().equals(gotDoc.getName()) || !expectedDoc.getNamespace().equals(gotDoc.getNamespace())) {
			return false;
		}
		String nodeName = expectedDoc.getName();
		Method m = findCompareMethod(nodeName);
		if(m != null) {
			try {
				return (Boolean) m.invoke(this,expectedDoc, gotDoc);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(checkText(nodeName) && ! expectedDoc.getText().equals(gotDoc.getText())) {
			return false;
		}
		return compareChildElems(expectedDoc, gotDoc);
	}
	
	protected boolean checkText(String nodeName) {
		return getCheckTextNames().contains(nodeName);
	}

	protected Set<String> getCheckTextNames() {
		return checkTextNames;
	}
	
	public void setCheckTextNames(String...names) {
		checkTextNames = new HashSet<String>(Arrays.asList(names));
	}

	protected boolean compareChildElems(Element expectedDoc, Element gotDoc){
		Set<Element> expectedNodes = new HashSet<Element>(expectedDoc.getChildren());
		Set<Element> gotNodes = new HashSet<Element>(gotDoc.getChildren());
Expect: for(Element expect : expectedNodes) {
			for(Element got : new HashSet<Element>(gotNodes)) {
				if(compareDocuments(expect, got)) {
					gotNodes.remove(got);
					continue Expect;
				}
			}
			System.out.println("Can't find "+expect+" with text "+expect.getText());
			return false;
		}
		return gotNodes.isEmpty();
	}

	protected Method findCompareMethod(String nodeName) {
		initMethods();
		return methods.get(nodeName);
	}

	protected void initMethods() {
		synchronized(methods) {
			if(inited)
				return;
			inited = true;
			for(Method method : getClass().getMethods()) {
				if(method.isAccessible()) {
					if(
						method.getName().startsWith("compare")
					) {
						String name = method.getName().substring(8);
						methods.put(name,method);
					}
				}
			}
		}
	}
}
