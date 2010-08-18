/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.TransformAction;

/**
 * Bean definition parser for transform action in test case.
 * 
 * @author Philipp Komninos
 */
public class TransformActionParser implements BeanDefinitionParser {

	/**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(TransformAction.class);
		
		DescriptionElementParser.doParse(element, beanDefinition);
		
		Element xmlDataElement = DomUtils.getChildElementByTagName(element, "xml-data");
		if (xmlDataElement != null) {
			beanDefinition.addPropertyValue("xmlData", DomUtils.getTextValue(xmlDataElement));
		}
		
		Element xmlResourceElement = DomUtils.getChildElementByTagName(element, "xml-resource");
		if (xmlResourceElement != null) {
			String filePath = xmlResourceElement.getAttribute("file");
            if (filePath.startsWith("classpath:")) {
            	beanDefinition.addPropertyValue("xmlResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
            	beanDefinition.addPropertyValue("xmlResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
            	beanDefinition.addPropertyValue("xmlResource", new FileSystemResource(filePath));
            }
		}
		
		Element xsltDataElement = DomUtils.getChildElementByTagName(element, "xslt-data");
		if (xsltDataElement != null) {
			beanDefinition.addPropertyValue("xsltData", DomUtils.getTextValue(xsltDataElement));
		}
		
		Element xsltResourceElement = DomUtils.getChildElementByTagName(element, "xslt-resource");
		if (xsltResourceElement != null) {
			String filePath = xsltResourceElement.getAttribute("file");
            if (filePath.startsWith("classpath:")) {
            	beanDefinition.addPropertyValue("xsltResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
            	beanDefinition.addPropertyValue("xsltResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
            	beanDefinition.addPropertyValue("xsltResource", new FileSystemResource(filePath));
            }
		}
		
		String variable = element.getAttribute("variable");
		if (StringUtils.hasText(variable)) {
			beanDefinition.addPropertyValue("targetVariable", variable);
		}
		
		return beanDefinition.getBeanDefinition();
	}

}
