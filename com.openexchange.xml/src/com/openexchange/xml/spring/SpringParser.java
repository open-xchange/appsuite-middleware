package com.openexchange.xml.spring;

import org.springframework.beans.factory.BeanFactory;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface SpringParser {
    public BeanFactory parseFile(String path, ClassLoader classLoader);
}
