/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.text.ParseException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class GroovyAction extends AbstractTestAction {

    /** Inline groovy script */
    private String script;

    /** External script file resource */
    private Resource fileResource;
    
    /** Class definition, for Groovy scripts, implements the ScriptExecutor Interface
     * for the execute method, which will be invoked, providing the Testcontext */
    private static final String GROOVY_CLASS_DEFINITION = 
    	"import com.consol.citrus.*\n" +
    	"import com.consol.citrus.variable.*\n" +
        "import com.consol.citrus.context.TestContext\n" +
        "import com.consol.citrus.script.GroovyAction.ScriptExecutor\n\n" +
        "public class GScript implements ScriptExecutor {\n" +
        "public void execute(TestContext context) {\n";
    
    /** Executes a script using the TestContext */
    public interface ScriptExecutor {
        public void execute(TestContext context);
    }

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GroovyAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            
            Class<?> groovyClass;
            
            String content;
            
            //get the script
            if (script != null) {
            	content = context.replaceDynamicContentInString(script);
            } else if (fileResource != null) {
            	content = context.replaceDynamicContentInString(FileUtils.readToString(fileResource));
            } else {
                throw new CitrusRuntimeException("Neither inline script nor " +
                		"external file resource is defined for bean. " +
                		"Can not execute groovy script.");
            }
            
            //create a Groovy class with the script and instantiate an object from it
            groovyClass = loader.parseClass(content);
            if (groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
            
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            
            //if there was no class, which implements ScriptExecuter, in the script:
            //put the Groovy class definition around the script
            if (!(groovyObject instanceof ScriptExecutor)) {
            	//throw error if there was an own class defined in the script
            	if (!groovyObject.getClass().getSimpleName().startsWith("script")) {
            		throw new CitrusRuntimeException("The class " + groovyObject.getClass().getSimpleName() + 
            				" of the Groovy-script has to implement com.consol.citrus.script.GroovyAction.ScriptExecutor" +
            				" with the execute(Testcontext context) method");
            	}
            	//if there was no class defined in the script, put the class definition around 
                groovyClass = loader.parseClass(GROOVY_CLASS_DEFINITION + content + "\n}}");
                groovyObject = (GroovyObject) groovyClass.newInstance();
            }
            //execute the Groovy script
            log.info("Executing Groovy script...");
            ((ScriptExecutor)groovyObject).execute(context);
            log.info("Groovy test action executed successfully");
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the groovy script code.
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Get the groovy script.
     * @return the script
     */
    public String getScript() {
        return script;
    }
    
    /**
     * Get the file resource.
     * @return the fileResource
     */
    public Resource getFileResource() {
        return fileResource;
    }

    /**
     * Set file resource.
     * @param fileResource the fileResource to set
     */
    public void setFileResource(Resource fileResource) {
        this.fileResource = fileResource;
    }
}
