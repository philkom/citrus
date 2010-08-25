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

package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a List of test actions,
 * that will be executed before TestSuite (e.g. for initializing tasks).
 * 
 * The List of test actions are injected via spring IoC container.
 *
 * @author Philipp Komninos
 * @since 2010
 *
 */
public class BeforeSuite extends ArrayList<TestAction> {
    /**
     * Set the List of test actions
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        super.addAll(actions);
    }
}
