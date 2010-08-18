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

package com.consol.citrus.actions;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.XMLUtils;


/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 *
 * @author Philipp Komninos
 * @since 2010
 */
public class TransformAction extends AbstractTestAction {
	
	/** Inline XML document */
	private String xmlData;
	
	/** External XML document resource */
	private Resource xmlResource;
	
	/** Inline XSLT document */
	private String xsltData;
	
	/** External XSLT document resource */
	private Resource xsltResource;
	
	/** Target variable for the result */
	private String targetVariable;
	
	/**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TransformAction.class);

	/**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
	@Override
	public void execute(TestContext context) {
		try {
			log.info("Starting transformation");
			//parse XML document and define XML source for transformation
			Document xmlDocument = null;
			if (xmlResource != null){
				xmlDocument = XMLUtils.parseMessagePayload(context.replaceDynamicContentInString(FileUtils.readToString(xmlResource)));
			} else if (xmlData != null){
				xmlDocument = XMLUtils.parseMessagePayload(context.replaceDynamicContentInString(xmlData));
			} else {
				throw new CitrusRuntimeException("Neither inline XML nor " +
                		"external file resource is defined for bean. " +
        				"Cannot transform XML document.");
			}
			if (xmlDocument == null) {
				throw new CitrusRuntimeException("Could not parse XML document.");
			}
			Source xmlSource = new DOMSource(xmlDocument);
			
			//parse XSLT document and define  XSLT source for transformation
			Document xsltDocument = null;
			if (xsltResource != null) {
				xsltDocument = XMLUtils.parseMessagePayload(context.replaceDynamicContentInString(FileUtils.readToString(xsltResource)));
			} else if (xsltData != null) {
				xsltDocument = XMLUtils.parseMessagePayload(context.replaceDynamicContentInString(xsltData));
			} else {
				throw new CitrusRuntimeException("Neither inline XSLT nor " +
                		"external file resource is defined for bean. " +
        				"Cannot transform XSLT document.");
			}
			if (xsltDocument == null) {
				throw new CitrusRuntimeException("Could not parse XSLT document.");
			}
			Source xsltSource = new DOMSource(xsltDocument);
			
			//create transformer
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer trans = transFact.newTransformer(xsltSource);
			
			//transform
			DOMResult result = new DOMResult();
			trans.transform(xmlSource, result);
			
			//save result to specified variable
			Document resultDoc = (Document) result.getNode();
			context.setVariable(targetVariable, XMLUtils.serialize(resultDoc));
			
			log.info("Transformation finished successfully");
		} catch (ParseException e) {
			throw new CitrusRuntimeException(e);
		} catch (IOException e) {
			throw new CitrusRuntimeException(e);
		} catch (TransformerConfigurationException e) {
			throw new CitrusRuntimeException(e);
		} catch (TransformerException e) {
			throw new CitrusRuntimeException(e);
		}
	}

	/**
	 * Set the XML document
	 * @param xmlData the xmlData to set
	 */
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	/**
	 * Set the XML document as resource
	 * @param xmlResource the xmlResource to set
	 */
	public void setXmlResource(Resource xmlResource) {
		this.xmlResource = xmlResource;
	}

	/**
	 * Set the XSLT document
	 * @param xsltData the xsltData to set
	 */
	public void setXsltData(String xsltData) {
		this.xsltData = xsltData;
	}

	/**
	 * Set the XSLT document as resource
	 * @param xsltResource the xsltResource to set
	 */
	public void setXsltResource(Resource xsltResource) {
		this.xsltResource = xsltResource;
	}

	/**
	 * Set the target variable for the result
	 * @param targetVariable the targetVariable to set
	 */
	public void setTargetVariable(String targetVariable) {
		this.targetVariable = targetVariable;
	}

}
