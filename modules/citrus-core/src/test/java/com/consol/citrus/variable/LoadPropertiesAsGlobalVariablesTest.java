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

package com.consol.citrus.variable;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class LoadPropertiesAsGlobalVariablesTest extends AbstractBaseTest {
    @Test
    public void testPropertyLoadingFromClasspath() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:com/consol/citrus/variable/loadtest.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
        
        Assert.assertTrue(globalVariables.getVariables().size() == 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
    }
    
//    @Test
//    public void testPropertyLoadingFromFilesystem() {
//        PropertyLoader propertyLoader = new PropertyLoader();
//        propertyLoader.setPropertyFiles(Collections.singletonList("file:src/test/resources/com/consol/citrus/variable/loadtest.properties"));
//        GlobalVariables globalVariables = new GlobalVariables();        
//        propertyLoader.setGlobalVariables(globalVariables);
//        
//        propertyLoader.loadPropertiesAsVariables();
//        
//        Assert.assertTrue(globalVariables.getVariables().size() == 1);
//        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
//    }
//    
//    @Test
//    public void testPropertyLoading() {
//        PropertyLoader propertyLoader = new PropertyLoader();
//        propertyLoader.setPropertyFiles(Collections.singletonList("src/test/resources/com/consol/citrus/variable/loadtest.properties"));
//        GlobalVariables globalVariables = new GlobalVariables();        
//        propertyLoader.setGlobalVariables(globalVariables);
//        
//        propertyLoader.loadPropertiesAsVariables();
//        
//        Assert.assertTrue(globalVariables.getVariables().size() == 1);
//        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
//    }
    
    @Test
    public void testOverrideExistingVariables() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:com/consol/citrus/variable/loadtest.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();
        globalVariables.getVariables().put("property.load.test", "InitialValue");
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
        
        Assert.assertTrue(globalVariables.getVariables().size() == 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
        Assert.assertFalse(globalVariables.getVariables().get("property.load.test").equals("InitialValue"));
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyFileDoesNotExist() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:file_not_exists.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();        
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
    }
    
    @Test
    public void testGlobalVariablesInPropertyFile() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:com/consol/citrus/variable/globalvariablestest.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();

        Assert.assertTrue(globalVariables.getVariables().get("text").equals("Hello Mickey Mouse!"));
    }
}
