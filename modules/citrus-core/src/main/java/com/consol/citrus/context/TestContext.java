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

package com.consol.citrus.context;

import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;
import com.consol.citrus.xml.xpath.XPathExpressionResult;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * Class holding and managing test variables. The test context also provides utility methods
 * for replacing dynamic content(variables and functions) in message payloads and headers.
 * 
 * @author Christoph Deppisch
 */
public class TestContext {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestContext.class);
    
    /** Local variables */
    protected Map<String, String> variables;
    
    /** Global variables */
    private GlobalVariables globalVariables;
    
    /** Function registry holding all available functions */
    private FunctionRegistry functionRegistry = new FunctionRegistry();
    
    /**
     * Default constructor
     */
    public TestContext() {
        variables = new LinkedHashMap<String, String>();
    }
    
    /**
     * Get the value for the given variable expression.
     * Expression can be a function or a simple variable name.
     * @param variableExpression expression to be parsed
     * @throws CitrusRuntimeException
     * @return value of variable as String
     */
    public String getVariable(final String variableExpression) {
        String value = null;

        if (variables.containsKey(VariableUtils.cutOffVariablesPrefix(variableExpression))) {
            value = (String)variables.get(VariableUtils.cutOffVariablesPrefix(variableExpression));
        }

        if (value == null) {
            throw new CitrusRuntimeException("Unknown variable '" + variableExpression + "'");
        }

        return value;
    }
    
    /**
     * Creates a new variable with the respective value
     * @param variableName, name of new variable
     * @param value, value of new variable
     * @throws CitrusRuntimeException
     * @return
     */
    public void setVariable(final String variableName, String value) {
        if (variableName == null || variableName.length() == 0 || VariableUtils.cutOffVariablesPrefix(variableName).length() == 0) {
            throw new CitrusRuntimeException("No variable name defined");
        }

        if (value == null) {
            throw new VariableNullValueException("Trying to set variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + ", but value is null");
        }

        if(log.isDebugEnabled()) {
            log.debug("Setting variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + " to value: " + value);
        }

        variables.put(VariableUtils.cutOffVariablesPrefix(variableName), value);
    }
    
    /**
     * Add all variables in map to local variables.
     * Existing variables will be overwritten.
     * @param context
     */
    public void addVariables(Map<String, String> variablesToSet) {
        for (Entry<String, String> entry : variablesToSet.entrySet()) {
            if (entry.getValue() != null) {
                setVariable(entry.getKey(), entry.getValue());
            } else {
                setVariable(entry.getKey(), "");
            }
        }
    }
    
    /**
     * Creates new variables from message payload.
     * The messageElements map holds XPath expressions (keys) and variable names (values). XPath expressions
     * are evaluated against the message's payload XML.
     * 
     * @param messageElements map holding variable names and XPath expressions.
     * @param message
     * @param nsContext
     * @throws UnknownElementException
     */
    public void createVariablesFromMessageValues(final Map<String, String> messageElements, 
            Message<?> message, NamespaceContext nsContext) throws UnknownElementException {
        if (CollectionUtils.isEmpty(messageElements)) {return;}

        if(log.isDebugEnabled()) {
            log.debug("Reading XML elements from document");
        }

        for (Entry<String, String> entry : messageElements.entrySet()) {
            String pathExpression = entry.getKey();
            String variableName = entry.getValue();

            if(log.isDebugEnabled()) {
                log.debug("Reading element: " + pathExpression);
            }
            
            Document doc = XMLUtils.parseMessagePayload(message.getPayload().toString());
            
            if (XPathUtils.isXPathExpression(pathExpression)) {
                if(nsContext == null) {
                    nsContext = new SimpleNamespaceContext();
                    ((SimpleNamespaceContext)nsContext).setBindings(XMLUtils.lookupNamespaces(message.getPayload().toString()));
                }
                
                XPathExpressionResult resultType = XPathExpressionResult.fromString(pathExpression, XPathExpressionResult.STRING);
                pathExpression = XPathExpressionResult.cutOffPrefix(pathExpression);
                
                String value = XPathUtils.evaluate(doc, pathExpression, nsContext, resultType);

                if(value == null) {
                    throw new CitrusRuntimeException("Not able to find value for expression: " + pathExpression);
                }
                
                setVariable(variableName, value);
            } else {
                Node node = XMLUtils.findNodeByName(doc, pathExpression);

                if (node == null) {
                    throw new UnknownElementException("No element found for expression" + pathExpression);
                }

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getFirstChild() != null) {
                        setVariable((String)messageElements.get(pathExpression), node.getFirstChild().getNodeValue());
                    } else {
                        setVariable((String)messageElements.get(pathExpression), "");
                    }
                } else {
                    setVariable((String)messageElements.get(pathExpression), node.getNodeValue());
                }
            }
        }
    }
    
    /**
     * Replaces variables in given map with respective variable values.
     * @param map
     */
    public Map<String, Object> replaceVariablesInMap(final Map<String, ?> map) {
        Map<String, Object> target = new HashMap<String, Object>();
        
        for (Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = (String)entry.getValue();

            // If value is a variable
            if (VariableUtils.isVariableName(value)) {
                // then replace variable name by variable value
                target.put(key, getVariable(value));
            } else if(functionRegistry.isFunction(value)) {
                target.put(key, FunctionUtils.resolveFunction(value, this));
            } else {
                target.put(key, value);
            }
        }
        
        return target;
    }
    
    /**
     * Replaces variables in list with respective variable values
     * @param list
     */
    public List<String> replaceVariablesInList(List<String> list) {
        List<String> variableFreeList = new ArrayList<String>();

        for (int i = 0; i < list.size(); i++) {
            String variable = list.get(i);
            if (VariableUtils.isVariableName(variable)) {
                // then replace variable by variable value
                variableFreeList.add(getVariable(variable));
            } else if(functionRegistry.isFunction(variable)) {
                variableFreeList.add(FunctionUtils.resolveFunction(variable, this));
            } else {
                variableFreeList.add(variable);
            }
        }

        return variableFreeList;
    }
    
    /**
     * Create new variables from message headers.
     *
     * @param extractHeaderValues map containing name value pairs 
     * where the keys represent header entry names and the value set variable names. 
     * @param receivedHeaderValues message header entries
     */
    public void createVariablesFromHeaderValues(final Map<String, String> extractHeaderValues, final Map<String, ?> receivedHeaderValues) throws UnknownElementException {
        if (extractHeaderValues== null || extractHeaderValues.isEmpty()) {return;}

        for (Entry<String, String> entry : extractHeaderValues.entrySet()) {
            String headerElementName = entry.getKey();
            String targetVariableName = entry.getValue();

            if (receivedHeaderValues.get(headerElementName) == null) {
                throw new UnknownElementException("Could not find header element " + headerElementName + " in received header");
            }

            setVariable(targetVariableName, receivedHeaderValues.get(headerElementName).toString());
        }
    }
    
    /**
     * Overwrite message elements in given message payload string.
     * 
     * Each key of the messageElements map represents a XPath expression.
     * Each value set entry represents the new value to set.
     *
     * @param messageElements map holding the elements to be overwritten
     * @param messagePayload
     * @throws CitrusRuntimeException
     * @throws UnknownElementException
     */
    public String replaceMessageValues(final Map<String, String> messageElements, String messagePayload) {
        Document doc = XMLUtils.parseMessagePayload(messagePayload);

        if (doc == null) {
            throw new CitrusRuntimeException("Not able to set message elements, because no XML ressource defined");
        }
        
        for (Entry<String, String> entry : messageElements.entrySet()) {
            String pathExpression = entry.getKey();
            String valueExpression = entry.getValue();

            if (VariableUtils.isVariableName(valueExpression)) {
                valueExpression = getVariable(valueExpression);
            } else if(functionRegistry.isFunction(valueExpression)) {
                valueExpression = FunctionUtils.resolveFunction(valueExpression, this);
            } 

            if (valueExpression == null) {
                throw new CitrusRuntimeException("Can not set null values in XML document - path expression is " + pathExpression);
            }
            
            Node node;
            if (XPathUtils.isXPathExpression(pathExpression)) {
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.setBindings(XMLUtils.lookupNamespaces(messagePayload));
                node = XPathUtils.evaluateAsNode(doc, pathExpression, nsContext);
            } else {
                node = XMLUtils.findNodeByName(doc, pathExpression);
            }

            if (node == null) {
                throw new UnknownElementException("Could not find element for expression" + pathExpression);
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getFirstChild() == null) {
                    node.appendChild(doc.createTextNode(valueExpression));
                } else {
                    node.getFirstChild().setNodeValue(valueExpression);
                }
            } else {
                node.setNodeValue(valueExpression);
            }
            
            if(log.isDebugEnabled()) {
                log.debug("Element " +  pathExpression + " was set to value: " + valueExpression);
            }
        }
        
        return XMLUtils.serialize(doc);
    }
    
    /**
     * Clear local variables.
     */
    public void clear() {
        variables.clear();
        variables.putAll(globalVariables.getVariables());
    }
    
    /**
     * Checks if variables are present right now.
     * @return boolean flag to mark existence
     */
    public boolean hasVariables() {
        return (this.variables != null && !this.variables.isEmpty());
    }
    
    /**
     * Setter for local variables.
     * @param variables
     */
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * Getter for local variables.
     * @return global variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * Get global variables.
     * @param globalVariables
     */
	public void setGlobalVariables(GlobalVariables globalVariables) {
		this.globalVariables = globalVariables;
		
		variables.putAll(globalVariables.getVariables());
	}

    /**
     * Set global variables.
     * @return the globalVariables
     */
    public Map<String, String> getGlobalVariables() {
        return globalVariables.getVariables();
    }
    
    /**
     * Method replacing variable declarations and functions in a string
     * @param str
     * @return
     * @throws ParseException
     */
    public String replaceDynamicContentInString(String str) throws ParseException {
        str = VariableUtils.replaceVariablesInString(str, this);
        str = FunctionUtils.replaceFunctionsInString(str, this);
        return str;
    }

    /**
     * Method replacing variable declarations and functions in a string, but adds quotes
     * to the replaced variable values.
     * @param str
     * @param enableQuoting
     * @return
     * @throws ParseException
     */
    public String replaceDynamicContentInString(String str, boolean enableQuoting) throws ParseException {
        str = VariableUtils.replaceVariablesInString(str, this, enableQuoting);
        str = FunctionUtils.replaceFunctionsInString(str, this, enableQuoting);
        return str;
    }

    /**
     * Get the current function registry.
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Set the function registry.
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}
