
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
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLCompare {

    private static Map<String, Method> methods = new HashMap<String, Method>();
    private static boolean inited;
    private Set<String> checkTextNames = Collections.emptySet();

    public boolean compare(final String expect, final String got) throws UnsupportedEncodingException, JDOMException, IOException {
        final SAXBuilder builder = new SAXBuilder();
        final Document expectedDoc = builder.build(new ByteArrayInputStream(expect.getBytes(com.openexchange.java.Charsets.UTF_8)));
        final Document gotDoc = builder.build(new ByteArrayInputStream(got.getBytes(com.openexchange.java.Charsets.UTF_8)));

        return compareDocuments(expectedDoc.getRootElement(), gotDoc.getRootElement());
    }

    public boolean compareDocuments(final Element expectedDoc, final Element gotDoc) {
        if (!expectedDoc.getName().equals(gotDoc.getName()) || !expectedDoc.getNamespace().equals(gotDoc.getNamespace())) {
            return false;
        }
        final String nodeName = expectedDoc.getName();
        final Method m = findCompareMethod(nodeName);
        if (m != null) {
            try {
                return ((Boolean) m.invoke(this, expectedDoc, gotDoc)).booleanValue();
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (checkText(nodeName) && !expectedDoc.getText().equals(gotDoc.getText())) {
            return false;
        }
        return compareChildElems(expectedDoc, gotDoc);
    }

    protected boolean checkText(final String nodeName) {
        return getCheckTextNames().contains(nodeName);
    }

    protected Set<String> getCheckTextNames() {
        return checkTextNames;
    }

    public void setCheckTextNames(final String... names) {
        checkTextNames = new HashSet<String>(Arrays.asList(names));
    }

    protected boolean compareChildElems(final Element expectedDoc, final Element gotDoc) {
        final Set<Element> expectedNodes = new HashSet<Element>(expectedDoc.getChildren());
        final Set<Element> gotNodes = new HashSet<Element>(gotDoc.getChildren());
        Expect: for (final Element expect : expectedNodes) {
            for (final Element got : new HashSet<Element>(gotNodes)) {
                if (compareDocuments(expect, got)) {
                    gotNodes.remove(got);
                    continue Expect;
                }
            }
            return false;
        }
        return gotNodes.isEmpty();
    }

    protected Method findCompareMethod(final String nodeName) {
        initMethods();
        return methods.get(nodeName);
    }

    protected void initMethods() {
        synchronized (methods) {
            if (inited) {
                return;
            }
            inited = true;
            for (final Method method : getClass().getMethods()) {
                if (method.isAccessible()) {
                    if (method.getName().startsWith("compare")) {
                        final String name = method.getName().substring(8);
                        methods.put(name, method);
                    }
                }
            }
        }
    }
}
