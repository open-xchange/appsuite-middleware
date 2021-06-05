/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.webdav.xml.resources;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import com.openexchange.webdav.action.behaviour.BehaviourLookup;
import com.openexchange.webdav.protocol.Multistatus;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatus;
import com.openexchange.webdav.protocol.util.Utils;

public class PropertiesMarshaller implements ResourceMarshaller {

    /** Pattern to extract problematic characters from verifier error messages */
    private static final Pattern HEX_CHARACTER_PATTERN = Pattern.compile("\\b0x([a-fA-F0-9]+)\\b");

	protected static final Namespace DAV_NS = Protocol.DAV_NS;
	protected static final Namespace DATE_NS = Namespace.getNamespace("b",  "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

	private String uriPrefix;

	private final String charset;

	@SuppressWarnings("unused") // used by children
    protected Multistatus<Iterable<WebdavProperty>> getProps(final WebdavResource resource) {
		return new Multistatus<Iterable<WebdavProperty>>();
	}

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertiesMarshaller.class);

	public PropertiesMarshaller(final String charset){
		this.charset = charset;
	}

	public PropertiesMarshaller(final String uriPrefix, final String charset) {
        this.uriPrefix = uriPrefix;
        if (!this.uriPrefix.endsWith("/")) {
            this.uriPrefix += "/";
        }
        this.charset = charset;
    }

	@Override
	public List<Element> marshal(final WebdavResource resource) throws WebdavProtocolException {
		final Element response =  new Element("response",DAV_NS);
		response.addContent(marshalHREF(resource.getUrl(), resource.isCollection()));
		if (resource.exists()) {
			final Multistatus<Iterable<WebdavProperty>> multistatus = getProps(resource);
			for(final int statusCode : multistatus.getStatusCodes()) {
				for(final WebdavStatus<Iterable<WebdavProperty>> status : multistatus.toIterable(statusCode)) {
					final Element propstat = new Element("propstat",DAV_NS);
					final Element prop = new Element("prop", DAV_NS);
                    if (status.getAdditional() != null) {
                        for (final WebdavProperty p : status.getAdditional()) {
                            if (p != null) {
                                prop.addContent(marshalProperty(p, resource.getProtocol()));
                            }
                        }
					}
					propstat.addContent(prop);
					propstat.addContent(marshalStatus(statusCode));
					response.addContent(propstat);
				}
			}
		} else {
			response.addContent(this.marshalStatus(HttpServletResponse.SC_NOT_FOUND));
		}
		return Arrays.asList(response);
	}

    public Element marshalHREF(WebdavPath uri, boolean trailingSlash) {
        final Element href = new Element("href", DAV_NS);
        final StringBuilder builder = new StringBuilder(uriPrefix);
        if (builder.charAt(builder.length() - 1) != '/') {
            builder.append('/');
        }
        for (final String component : uri) {
            builder.append(escape(component)).append('/');
        }
        if (!trailingSlash) {
            builder.setLength(builder.length() - 1);
        }
        href.setText(builder.toString());
        return href;
    }

	private String escape(final String string) {
		final PropfindResponseUrlEncoder encoder = BehaviourLookup.getInstance().get(PropfindResponseUrlEncoder.class);
		if (null != encoder) {
			return encoder.encode(string);
		}
		try {
			return URLEncoder.encode(string,charset).replaceAll("\\+","%20");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.toString());
			return string;
		}
	}

	public Element marshalStatus(final int s) {
		final Element status = new Element("status",DAV_NS);
		final StringBuilder content = new StringBuilder("HTTP/1.1 ");
		content.append(s);
		content.append(' ');
		content.append(Utils.getStatusString(s));
		status.setText(content.toString());
		return status;
	}

	public Element marshalProperty(WebdavProperty property, Protocol protocol) {
		Element propertyElement = new Element(property.getName(), getNamespace(property));
        if (null == property.getValue() && null == property.getChildren()) {
			return propertyElement;
		}
		if (property.isXML()) {
			try {
                StringBuilder xmlBuilder = new StringBuilder("<FKR:fakeroot xmlns:FKR=\"http://www.open-xchange.com/webdav/fakeroot\" xmlns:D=\"DAV:\"");
                if (false == "DAV:".equals(property.getNamespace())) {
                    xmlBuilder.append(" xmlns=\"").append(property.getNamespace()).append('"');
                }
                List<Namespace> namespaces = protocol.getAdditionalNamespaces();
                for (Namespace namespace : namespaces) {
                    xmlBuilder.append(" xmlns:").append(namespace.getPrefix()).append("=\"").append(namespace.getURI()).append('"');
                }
                xmlBuilder.append('>').append(property.getValue()).append("</FKR:fakeroot>");
                final Document doc = buildDocument(xmlBuilder.toString());
				propertyElement.setContent(doc.getRootElement().cloneContent());
                Map<String, String> attributes = property.getAttributes();
                if (null != attributes) {
                    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                        propertyElement.setAttribute(attribute.getKey(), attribute.getValue());
                    }
                }
			} catch (JDOMException e) {
				// NO XML
                LOG.error("", e);
				propertyElement.setText(property.getValue());
			} catch (IOException e) {
				LOG.error("", e);
			}
		} else {
			if (property.isDate()) {
				propertyElement.setAttribute("dt", "dateTime.rfc1123", DATE_NS);
			}
			propertyElement.setText(property.getValue());
            if (null != property.getChildren()) {
                propertyElement.addContent(property.getChildren());
            }
		}
		return propertyElement;
	}
	
    private static Document buildDocument(String content) throws JDOMException, IOException {
        try {
            return new SAXBuilder().build(new StringReader(content));
        } catch (JDOMParseException e) {
            String sanitizedContent = replaceUnallowedCharacters(content, "");
            if (false == Objects.equals(content, sanitizedContent)) {
                return buildDocument(sanitizedContent);
            }
            throw e;
        }
    }

    private static String replaceUnallowedCharacters(String value, String replacement) {
        if (null == value) {
            return value;
        }
        String result = org.jdom2.Verifier.checkCharacterData(value);
        if (null != result) {
            Matcher matcher = HEX_CHARACTER_PATTERN.matcher(result);
            if (matcher.find()) {
                try {
                    char character = (char) Integer.parseInt(matcher.group(1), 16);
                    return value.replaceAll(String.valueOf(character), replacement);
                } catch (Exception e) {
                    // ignore
                    LOG.trace("", e);
                }
            }
        }
        return value;
    }

	private Namespace getNamespace(final WebdavProperty property) {
		final String namespace = property.getNamespace();
		if (namespace.equals("DAV:")) {
			return DAV_NS;
		}
		return Namespace.getNamespace(namespace);
	}
}
