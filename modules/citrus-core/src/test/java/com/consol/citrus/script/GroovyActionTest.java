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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionTest extends AbstractBaseTest {
    
    @Test
    public void testScript() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("println 'Hello TestFramework!'");
        bean.execute(context);
    }
    
    @Test
    public void testFileResource() {
        GroovyAction bean = new GroovyAction();
        bean.setFileResource(new ClassPathResource("com/consol/citrus/script/example.groovy"));
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptFailure() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("Some wrong script");
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testFileNotFound() {
        GroovyAction bean = new GroovyAction();
        bean.setFileResource(new FileSystemResource("some/wrong/path/test.groovy"));
        bean.execute(context);
    }
    
    @Test
    public void testScriptWithClassDefinition() {
        GroovyAction bean = new GroovyAction();
        StringBuilder sb = new StringBuilder();
        sb.append("import com.consol.citrus.*\n");
        sb.append("import com.consol.citrus.variable.*\n");
        sb.append("import com.consol.citrus.context.TestContext\n");
        sb.append("import com.consol.citrus.script.GroovyAction.ScriptExecutor\n\n");
        sb.append("public class GScript implements ScriptExecutor {\n");
        sb.append("public void execute(TestContext context) {\n");
        sb.append("context.setVariable(\"var\", \"Script with class definition test successful.\")\n");
        sb.append("println context.getVariable(\"var\")\n");
        sb.append("}}");
        bean.setScript(sb.toString());
        bean.execute(context);
    }
    
    @Test
    public void testScriptWithoutClassDefinition() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("context.setVariable(\"var\", \"Script without class definition test successful.\")\n" +
        		"println context.getVariable(\"var\")");
        bean.execute(context);
    }
}
