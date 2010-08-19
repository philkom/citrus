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

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;

/**
 * Abstract base class for all containers holding several embedded test actions.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractActionContainer extends AbstractTestAction implements TestActionContainer {
    /** List of nested actions */
    protected List<TestAction> actions = new ArrayList<TestAction>();

    /** Last executed action for error reporting reasons */
    private TestAction lastExecutedAction;
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#setActions(java.util.List)
     */
    public void setActions(List<TestAction> actions) {
        this. actions = actions;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getActions()
     */
    public List<TestAction> getActions() {
        return actions;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionCount()
     */
    public long getActionCount() {
        return actions.size();
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#addTestAction(com.consol.citrus.TestAction)
     */
    public void addTestAction(TestAction action) {
        actions.add(action);
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionIndex(com.consol.citrus.TestAction)
     */
    public int getActionIndex(TestAction action) {
        return actions.indexOf(action);
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getLastExecutedAction()
     */
    public TestAction getLastExecutedAction() {
        return lastExecutedAction;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#setLastExecutedAction(com.consol.citrus.TestAction)
     */
    public void setLastExecutedAction(TestAction action) {
        this.lastExecutedAction = action;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getTestAction(int)
     */
    public TestAction getTestAction(int index) {
        return actions.get(index);
    }
}
