package com.openexchange.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLCompare {

		
	private static Map<String,Method>  methods = new HashMap<String,Method>();
	private static boolean inited;
	private static DocumentBuilder builder = null;
	private Set<String> checkTextNames = Collections.EMPTY_SET;
	
	public boolean compare(String expect, String got) throws ParserConfigurationException, SAXException, IOException{
		if(builder == null)
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document expectedDoc = builder.parse(new ByteArrayInputStream(expect.getBytes("UTF-8")));
		Document gotDoc = builder.parse(new ByteArrayInputStream(got.getBytes("UTF-8")));
		
		return compareDocuments(expectedDoc, gotDoc);
	}
	
	public boolean compareDocuments(Node expectedDoc, Node gotDoc) {
		if(!expectedDoc.getNodeName().equals(gotDoc.getNodeName())) {
			return false;
		}
		String nodeName = expectedDoc.getNodeName();
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
		if(checkText(nodeName) && ! expectedDoc.getTextContent().equals(gotDoc.getTextContent())) {
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

	protected boolean compareChildElems(Node expectedDoc, Node gotDoc){
		Set<Element> expectedNodes = elementSet(expectedDoc.getChildNodes());
		Set<Element> gotNodes = elementSet(gotDoc.getChildNodes());
Expect: for(Element expect : expectedNodes) {
			for(Element got : new HashSet<Element>(gotNodes)) {
				if(compareDocuments(expect, got)) {
					gotNodes.remove(got);
					continue Expect;
				}
			}
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

	protected final Set<Element> elementSet(NodeList childNodes) {
		Set<Element> elements = new HashSet<Element>();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE)
				elements.add((Element) node);
		}
		return elements;
	}

}
