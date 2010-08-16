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
