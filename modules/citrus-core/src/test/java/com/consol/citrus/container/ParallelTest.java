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

package com.consol.citrus.container;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ParallelTest extends AbstractBaseTest {

    @Test
    public void testSingleAction() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test
    public void testParallelMultipleActions() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(action);
        actionList.add(new EchoAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test
    public void testParallelActions() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new EchoAction());
        actionList.add(new EchoAction());
        
        SleepAction sleep = new SleepAction();
        sleep.setDelay("0.3");
        actionList.add(sleep);
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOneActionThatIsFailing() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOnlyActionFailingActions() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        actionList.add(new FailAction());
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSingleFailingAction() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new FailAction());
        actionList.add(new EchoAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSomeFailingActions() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new FailAction());
        actionList.add(action);
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
}
