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

package com.consol.citrus.util;

import org.codehaus.groovy.control.CompilationFailedException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * Class to provide utilities for Groovy.
 * 
 * @author Philipp Komninos
 * @since 2010
 */
public class GroovyUtils {
	
	/**
	 * Definition for the Groovy MarkupBuilder
	 */
	private static final String MARKUPBUILDER_DEFINITION=
		"import groovy.xml.MarkupBuilder\n\n" +
		"def writer = new StringWriter()\n" +
		"def xml = new MarkupBuilder(writer)\n";
	
	/**
	 * Definition for the Groovy Slurper
	 */
	private static final String SLURPER_DEFINITION=
		"import com.consol.citrus.*\n" +
    	"import com.consol.citrus.variable.*\n" +
        "import com.consol.citrus.context.TestContext\n" +
        "import com.consol.citrus.util.GroovyUtils.ValidationScriptExecutor\n" +
        "import groovy.util.XmlSlurper\n" +
        "public class ValidationScript implements ValidationScriptExecutor{\n" +
        "public void validate(String receivedMessage, TestContext context){\n" +
		"def root = new XmlSlurper().parseText(receivedMessage)\n";
	
	/**
	 * Executes a validation-script
	 */
	public interface ValidationScriptExecutor {
		public void validate(String receivedMessage, TestContext context);
	}
	
	/**
     * Converts a Groovy MarkupBuilder script to a XML document and returns it as String.
     * 
     * @param scriptData
     * @return
     */
	public static String convertMarkupBuilderScript(String scriptData) {
		try {
			ClassLoader parent = GroovyUtils.class.getClassLoader(); 
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Class<?> groovyClass = loader.parseClass(MARKUPBUILDER_DEFINITION + scriptData + "\nreturn writer.toString()");
			if(groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			Object[] args = {};
			return (String) groovyObject.invokeMethod("run", args);
		} catch (CompilationFailedException e) {
			throw new CitrusRuntimeException(e);
		} catch (InstantiationException e) {
			throw new CitrusRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new CitrusRuntimeException(e);
		}
	}

	/**
     * Runs the validation-script, which provides the use of Groovy Slurper and the Testcontext.
     * 
     * @param validationScript
     * @param receivedMessage
     * @param context
     * @return
     */
	public static void validateScript(String validationScript, String receivedMessage, TestContext context) {
		try {
			ClassLoader parent = GroovyUtils.class.getClassLoader(); 
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Class<?> groovyClass = loader.parseClass(SLURPER_DEFINITION + validationScript + "\n}}");
			if(groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			((ValidationScriptExecutor)groovyObject).validate(receivedMessage, context);
		} catch (CompilationFailedException e) {
			throw new CitrusRuntimeException(e);
		} catch (InstantiationException e) {
			throw new CitrusRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new CitrusRuntimeException(e);
		}
	}
}
