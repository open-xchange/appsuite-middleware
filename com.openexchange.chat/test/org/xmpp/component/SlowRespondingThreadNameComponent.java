/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmpp.component;

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

/**
 * An {@link AbstractComponent} implementation that features debug
 * functionality, intended to be used by unit tests.
 *
 * This component will respond to IQ-get requests containing a child element
 * escaped by the namespace <tt>tinder:debug</tt>. If the child element name is
 * <tt>threadname</tt>, a response will be generated that reports the name of
 * the thread used to process the stanza, as shown:
 *
 * <pre>
 * &lt;iq type='get' id='debug_1'&gt;
 *   &lt;threadname xmlns='tinder:debug'/&gt;
 * &lt;/iq&gt;
 * </pre>
 *
 * <pre>
 * &lt;iq type='result' id='debug_1'&gt;
 *   &lt;threadname xmlns='tinder:debug'&gt;consumer-thread-34&lt;/threadname&gt;
 * &lt;/iq&gt;
 * </pre>
 *
 * If the element name is <tt>slowresponse</tt>, an empty response will be
 * generated 4000 milliseconds after the request was delivered to the component.
 *
 * <pre>
 * &lt;iq type='get' id='debug_2'&gt;
 *   &lt;slowresponse xmlns='tinder:debug'/&gt;
 * &lt;/iq&gt;
 * </pre>
 *
 * <pre>
 * &lt;iq type='result' id='debug_2'/&gt;
 * </pre>
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class SlowRespondingThreadNameComponent extends DummyAbstractComponent {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SlowRespondingThreadNameComponent.class));

	public static final String DEBUG_NAMESPACE = "tinder:debug";
	public static final String ELEMENTNAME_SLOWRESPONSE = "slowresponse";
	public static final String ELEMENTNAME_THREADNAME = "threadname";

	/**
	 * Processes the tinder:debug requests.
	 */
	@Override
	protected IQ handleIQGet(final IQ request) throws Exception {
		final Element element = request.getChildElement();
		if (!DEBUG_NAMESPACE.equals(element.getNamespaceURI())) {
			LOG.debug(MessageFormat.format("Can not process {0}", request.toXML()));
			return null;
		}

		if (ELEMENTNAME_SLOWRESPONSE.equals(element.getName())) {
		    LOG.debug(MessageFormat.format("Waiting 4000 millis before responding to: {}", request
					.toXML()));
			Thread.sleep(4000);
			LOG.debug(MessageFormat.format("Responding to {0} now.", request.toXML()));
			return IQ.createResultIQ(request);
		}

		if (ELEMENTNAME_THREADNAME.equals(element.getName())) {
			final String threadName = Thread.currentThread().getName();
			final IQ response = IQ.createResultIQ(request);
			response.setChildElement(ELEMENTNAME_THREADNAME, DEBUG_NAMESPACE)
					.addText(threadName);
			LOG.debug(MessageFormat.format("Responding to {0} with {1}", request.toXML(), response
					.toXML()));
			return response;
		}

		LOG.debug(MessageFormat.format("Cannot process {0}", request.toXML()));
		return null;
	}
}
