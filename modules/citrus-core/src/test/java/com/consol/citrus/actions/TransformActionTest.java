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

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Philipp Komninos
 */
public class TransformActionTest extends AbstractBaseTest {
	
	@Test
	public void testTransform(){
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		StringBuilder xsltDoc = new StringBuilder();
		xsltDoc.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
		xsltDoc.append("<xsl:template match=\"/\">\n");
		xsltDoc.append("<html>\n");
		xsltDoc.append("<body>\n");
		xsltDoc.append("<h2>Test Request</h2>\n");
		xsltDoc.append("<p>Message: <xsl:value-of select=\"TestRequest/Message\"/></p>\n");
		xsltDoc.append("</body>\n");  
		xsltDoc.append("</html>\n");
		xsltDoc.append("</xsl:template>\n");
		xsltDoc.append("</xsl:stylesheet>");
		transformAction.setXsltData(xsltDoc.toString());
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		StringBuilder transformedDoc = new StringBuilder();
		transformedDoc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		transformedDoc.append("<html>\n");
		transformedDoc.append("    <body>\n");
		transformedDoc.append("        <h2>Test Request</h2>\n");
		transformedDoc.append("        <p>Message: Hello World!</p>\n");
		transformedDoc.append("    </body>\n");
		transformedDoc.append("</html>\n");
		
		Assert.assertEquals(context.getVariable("var"), transformedDoc.toString());
	}
	
	@Test
	public void testTransformResource(){
		TransformAction transformAction = new TransformAction();
		transformAction.setXmlResource(new ClassPathResource("test-request-payload.xml", TransformActionTest.class));
		transformAction.setXsltResource(new ClassPathResource("test-transform.xslt", TransformActionTest.class));
		transformAction.setTargetVariable("var");
		
		transformAction.execute(context);
		
		StringBuilder transformedDoc = new StringBuilder();
		transformedDoc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		transformedDoc.append("<html>\n");
		transformedDoc.append("    <body>\n");
		transformedDoc.append("        <h2>Test Request</h2>\n");
		transformedDoc.append("        <p>Message: Hello World!</p>\n");
		transformedDoc.append("    </body>\n");
		transformedDoc.append("</html>\n");
		
		Assert.assertEquals(context.getVariable("var"), transformedDoc.toString());
	}
}
