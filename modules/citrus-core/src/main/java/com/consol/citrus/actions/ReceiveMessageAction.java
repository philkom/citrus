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

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.GroovyUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.DefaultXMLMessageValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.XmlValidationContext;

/**
 * This action receives messages from a service destination. Action uses a {@link MessageReceiver} 
 * to receive the message, this means that action is independent from any message transport.
 * 
 * The received message is validated using a {@link MessageValidator} supporting expected 
 * control message payload and header templates.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class ReceiveMessageAction extends AbstractTestAction {
    /** Map extracting message elements to variables */
    private Map<String, String> extractMessageElements = new HashMap<String, String>();

    /** Map extracting header values to variables */
    private Map<String, String> extractHeaderValues = new HashMap<String, String>();

    /** Build message selector with name value pairs */
    private Map<String, String> messageSelector = new HashMap<String, String>();

    /** Select messages via message selector string */
    private String messageSelectorString;

    /** Message receiver */
    private MessageReceiver messageReceiver;
    
    /** Receive timeout */
    private long receiveTimeout = 0L;

    /** Control message payload defined in external file resource */
    private Resource messageResource;

    /** Inline control message payload */
    private String messageData;
    
    /** Control message payload defined in external file resource as Groovy MarkupBuilder script */
    private Resource scriptResource;

    /** Inline control message payload as Groovy MarkupBuilder script */
    private String scriptData;
    
    /** Overwrites message elements before validating (via XPath expressions) */
    private Map<String, String> messageElements = new HashMap<String, String>();

    /** MessageValidator responsible for message validation */
    private MessageValidator validator;
    
    /** Validation context holding information like expected message payload, 
     * ignored elements and so on */
    private XmlValidationContext validationContext = new XmlValidationContext();
    
    /** Validation (Groovy) script for message validation,
     * used instead of XML message or Groovy MarkupBuilder script */
    private String validationScript;
    
    /** Validation (Groovy) script for message validation
     * used instead of XML message or Groovy MarkupBuilder script
     * defined in external file*/
    private Resource validationScriptResource;
    
    /** Indicates the type of the validation script,
     * which supports only Groovy at the moment
     */
    private String validationScriptType = "groovy";
    
    /** XML namespace declaration used for XPath expression evaluation */
    private Map<String, String> namespaces = new HashMap<String, String>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReceiveMessageAction.class);

    /**
     * Method receives a message via {@link MessageReceiver} instance
     * constructs a validation context and starts the message validation
     * via {@link MessageValidator}.
     * 
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        Message<?> receivedMessage;
        
        try {
            //receive message either selected or plain with message receiver
            if (StringUtils.hasText(messageSelectorString)) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting message selector: '" + messageSelectorString + "'");
                }
                
                if(receiveTimeout > 0) {
                    receivedMessage = messageReceiver.receiveSelected(
                            context.replaceDynamicContentInString(messageSelectorString), 
                            receiveTimeout);
                } else {
                    receivedMessage = messageReceiver.receiveSelected(
                            context.replaceDynamicContentInString(messageSelectorString));
                }
            } else if (!CollectionUtils.isEmpty(messageSelector)) {
                String constructedMessageSelector = MessageSelectorBuilder.fromKeyValueMap(
                        context.replaceVariablesInMap(messageSelector)).build();
                        
                if (log.isDebugEnabled()) {
                    log.debug("Setting message selector: '" + constructedMessageSelector + "'");
                }
                
                if(receiveTimeout > 0) {
                    receivedMessage = messageReceiver
                            .receiveSelected(constructedMessageSelector, receiveTimeout);
                } else {
                    receivedMessage = messageReceiver
                            .receiveSelected(constructedMessageSelector);
                }
            } else {
                receivedMessage = receiveTimeout > 0 ? messageReceiver.receive(receiveTimeout) : messageReceiver.receive();
            }

            if (receivedMessage == null) {
                throw new CitrusRuntimeException("Received message is null!");
            }

            //save variables from header values
            context.createVariablesFromHeaderValues(extractHeaderValues, receivedMessage.getHeaders());
            
            //set namespaces to validate
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
            Map<String, String> dynamicBindings = XMLUtils.lookupNamespaces(receivedMessage.getPayload().toString());
            if(!namespaces.isEmpty()) {
                //dynamic binding of namespaces declarations in root element of received message
                for (Entry<String, String> binding : dynamicBindings.entrySet()) {
                    //only bind namespace that is not present in explicit namespace bindings
                    if(!namespaces.containsValue(binding.getValue())) {
                        nsContext.bindNamespaceUri(binding.getKey(), binding.getValue());
                    }
                }
                //add explicit namespace bindings
                nsContext.setBindings(namespaces);
            } else {
                nsContext.setBindings(dynamicBindings);
            }
            
            validationContext.setNamespaceContext(nsContext);
            
            //save variables from message payload
            context.createVariablesFromMessageValues(extractMessageElements, receivedMessage, validationContext.getNamespaceContext());
            
            //validation with XML message data or Groovy MarkupBuilder script
            if (validationScript == null) {
	            //check if empty message was expected
		        if (receivedMessage.getPayload() == null || receivedMessage.getPayload().toString().length() == 0) {
		            if (messageResource == null && (messageData == null || messageData.length() == 0) &&
		                scriptResource == null && (scriptData == null || scriptData.length() == 0)) {
		                log.info("Received message body is empty as expected - therefore no message validation");
		                return;
		            } else {
		                throw new CitrusRuntimeException("Validation error: Received message body is empty");
		            }
		        }
		        
		        //construct control message payload
		        String expectedMessagePayload = "";
		        if (messageResource != null) {
		            expectedMessagePayload = context.replaceDynamicContentInString(FileUtils.readToString(messageResource));
		        } else if (messageData != null){
		            expectedMessagePayload = context.replaceDynamicContentInString(messageData);
		        } else if (scriptResource != null){
		            expectedMessagePayload = GroovyUtils.convertMarkupBuilderScript(context.replaceDynamicContentInString(FileUtils.readToString(scriptResource)));
		        } else if (scriptData != null){
		            expectedMessagePayload = GroovyUtils.convertMarkupBuilderScript(context.replaceDynamicContentInString(scriptData));
		        }
		        
		        if (StringUtils.hasText(expectedMessagePayload)) {
		            expectedMessagePayload = context.replaceMessageValues(messageElements, expectedMessagePayload);
		            Message<String> expectedMessage = MessageBuilder.withPayload(expectedMessagePayload).build();
		            
		            validationContext.setExpectedMessage(expectedMessage);
		        }
		        
		        //validate message
		        validateMessage(receivedMessage, context);
            } else { //validation with validation-script
            	log.info("Starting message validation with validation-script");
            	//validate schema,namespaces and message header
            	DefaultXMLMessageValidator xmlValidator = (DefaultXMLMessageValidator) validator;
            	if(validationContext.isSchemaValidation()) {
            		xmlValidator.validateXMLSchema(receivedMessage);
            		xmlValidator.validateDTD(validationContext.getDTDResource(), receivedMessage);
                }
            	xmlValidator.validateNamespaces(validationContext.getExpectedNamespaces(), receivedMessage);
            	xmlValidator.validateMessageHeader(validationContext.getExpectedMessageHeaders(), receivedMessage.getHeaders(), context);
                //validate groovy script
            	if (validationScriptType.equals("groovy")) {
					String script = "";
            		if (validationScriptResource != null) {
            			script = context.replaceDynamicContentInString(FileUtils.readToString(validationScriptResource));
            		} else if (validationScript != null) {
            			script = context.replaceDynamicContentInString(validationScript);
            		}
                    GroovyUtils.validateScript(script, receivedMessage.getPayload().toString(), context);
					log.info("Validation with Groovy validation-script finished successfully");					
				}
            	else {
					throw new CitrusRuntimeException("Validation-script type " + validationScriptType + " is unknown, only groovy is supported");
				}
            }
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Override this message if you want to add additional message validation
     * @param receivedMessage
     */
    protected void validateMessage(Message<?> receivedMessage, TestContext context) {
        validator.validateMessage(receivedMessage, context, validationContext);
    }

    /**
     * Setter for validating elements.
     * @param messageElements
     */
    public void setValidateMessageElements(Map<String, String> messageElements) {
        validationContext.setExpectedMessageElements(messageElements);
    }

    /**
     * Setter for ignored message elements.
     * @param ignoreMessageElements
     */
    public void setIgnoreMessageElements(Set<String> ignoredMessageElements) {
        validationContext.setIgnoreMessageElements(ignoredMessageElements);
    }

    /**
     * Setter for expected namespaces.
     * @param expectedNamespaces the expectedNamespaces to set
     */
    public void setExpectedNamespaces(Map<String, String> expectedNamespaces) {
        validationContext.setExpectedNamespaces(expectedNamespaces);
    }

    /**
     * Extract variables from header.
     * @param getHeaderValues the getHeaderValues to set
     */
    public void setExtractHeaderValues(Map<String, String> extractHeaderValues) {
        this.extractHeaderValues = extractHeaderValues;
    }

    /**
     * Extract variables from message payload.
     * @param extractMessageElements the extractMessageElements to set
     */
    public void setExtractMessageElements(Map<String, String> extractMessageElements) {
        this.extractMessageElements = extractMessageElements;
    }

    /**
     * Set expected control header values.
     * @param headerValues the headerValues to set
     */
    public void setHeaderValues(Map<String, Object> headerValues) {
        validationContext.setExpectedMessageHeaders(MessageBuilder.withPayload("")
                .copyHeaders(headerValues).build().getHeaders());
    }

    /**
     * Set message payload as inline data.
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    /**
     * Message payload as external file resource.
     * @param messageResource the messageResource to set
     */
    public void setMessageResource(Resource messageResource) {
        this.messageResource = messageResource;
    }
    
    /**
     * Set message payload data as inline Groovy MarkupBuilder script.
     * @param scriptData the scriptData to set
     */
    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    /**
     * Message payload as external Groovy MarkupBuilder script file resource.
     * @param scriptResource the scriptResource to set
     */
    public void setScriptResource(Resource scriptResource) {
        this.scriptResource = scriptResource;
    }

    /**
     * Check if header values for extraction are present.
     * @return boolean flag to mark existence
     */
    public boolean hasExtractHeaderValues() {
        return (this.extractHeaderValues != null && !this.extractHeaderValues.isEmpty());
    }

    /**
     * Setter for messageSelector.
     * @param messageSelector
     */
    public void setMessageSelector(Map<String, String> messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * Set message selector string.
     * @param messageSelectorString
     */
    public void setMessageSelectorString(String messageSelectorString) {
        this.messageSelectorString = messageSelectorString;
    }

    /**
     * Set validator instance.
     * @param validator the validator to set
     */
    public void setValidator(MessageValidator validator) {
        this.validator = validator;
    }
    
    /**
     * Set the validation-script.
     * @param validationScript the validationScript to set
     */
    public void setValidationScript(String validationScript){
    	this.validationScript = validationScript;
    }

	/**
	 * Set the validation-script as resource
	 * @param validationScriptResource the validationScriptResource to set
	 */
	public void setValidationScriptResource(Resource validationScriptResource) {
		this.validationScriptResource = validationScriptResource;
	}
	
    /**
     * Set the type of the validation script.
     * @param validationScriptType the validationScriptType to set
     */
    public void setValidationScriptType(String validationScriptType){
    	this.validationScriptType = validationScriptType;
    }
    
    /**
     * List of expected namespaces.
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get expected namespaces.
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set message receiver instance.
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * Get the message receiver.
     * @return the messageReceiver
     */
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * Enable schema validation.
     * @param enableSchemaValidation the schemaValidation to set
     */
    public void setSchemaValidation(boolean enableSchemaValidation) {
        validationContext.setSchemaValidation(enableSchemaValidation);
    }

    /**
     * Set the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Set message elements to overwrite before validation.
     * @param messageElements the messageElements to set
     */
    public void setMessageElements(Map<String, String> messageElements) {
        this.messageElements = messageElements;
    }
}
