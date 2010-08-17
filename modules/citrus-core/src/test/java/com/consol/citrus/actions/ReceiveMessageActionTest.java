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

import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.DefaultXMLMessageValidator;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends AbstractBaseTest {
	
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
    
    @Test
    @SuppressWarnings("unchecked")
	public void testReceiveMessageWithMessagePayloadData() {
		ReceiveMessageAction receiveAction = new ReceiveMessageAction();
		receiveAction.setMessageReceiver(messageReceiver);
		receiveAction.setValidator(validator);
		receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		
		Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
		reset(messageReceiver);
		expect(messageReceiver.receive()).andReturn(controlMessage).once();
		replay(messageReceiver);
		
		receiveAction.execute(context);
		
		verify(messageReceiver);
	}
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessagePayloadResource() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageResource(new ClassPathResource("test-request-payload.xml", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
	public void testReceiveMessageWithMessagePayloadScriptData() {
		ReceiveMessageAction receiveAction = new ReceiveMessageAction();
		receiveAction.setMessageReceiver(messageReceiver);
		receiveAction.setValidator(validator);
		StringBuilder sb = new StringBuilder();
		sb.append("xml.TestRequest(){\n");
		sb.append("Message('Hello World!')\n");
		sb.append("}");
		receiveAction.setScriptData(sb.toString());
		
		Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
		reset(messageReceiver);
		expect(messageReceiver.receive()).andReturn(controlMessage).once();
		replay(messageReceiver);
		
		receiveAction.execute(context);
		
		verify(messageReceiver);
	}
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessagePayloadScriptResource() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setScriptResource(new ClassPathResource("test-request-payload.groovy", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessagePayloadDataVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessagePayloadResourceVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageResource(new ClassPathResource("test-request-payload-with-variables.xml", SendMessageActionTest.class));
        
        context.setVariable("myText", "Hello World!");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessagePayloadResourceFunctionsSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageResource(new ClassPathResource("test-request-payload-with-functions.xml", SendMessageActionTest.class));
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageOverwriteMessageElementsXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");
        receiveAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageOverwriteMessageElementsDotNotation() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");
        receiveAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageOverwriteMessageElementsXPathWithNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        
        receiveAction.setMessageData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");
        receiveAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
        		"<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageOverwriteMessageElementsXPathWithNestedNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        
        receiveAction.setMessageData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">?</ns1:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");
        receiveAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
        		"<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageOverwriteMessageElementsXPathWithDefaultNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        
        receiveAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");
        receiveAction.setMessageElements(overwriteElements);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest xmlns=\"http://citrusframework.org/unittest\"><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessageHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        receiveAction.setHeaderValues(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithMessageHeadersVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        context.setVariable("myOperation", "sayHello");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        receiveAction.setHeaderValues(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithUnknownVariablesInMessageHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        receiveAction.setHeaderValues(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable '${myOperation}'");
            return;
        }
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithUnknownVariableInMessagePayload() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Operation", "myOperation");
        receiveAction.setExtractHeaderValues(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertEquals(context.getVariable("myOperation"), "sayHello");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithValidateMessageElementsFromMessageXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/TestRequest/Message", "Hello World!");
        receiveAction.setValidateMessageElements(messageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithValidateMessageElementsXPathDefaultNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/:TestRequest/:Message", "Hello World!");
        receiveAction.setValidateMessageElements(messageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
        		"<Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");
        receiveAction.setValidateMessageElements(messageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithValidateMessageElementsXPathNestedNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");
        receiveAction.setValidateMessageElements(messageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceBindings() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/pfx:TestRequest/pfx:Message", "Hello World!");
        receiveAction.setValidateMessageElements(messageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        receiveAction.setNamespaces(namespaces);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromMessageXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/TestRequest/Message", "messageVar");
        receiveAction.setExtractMessageElements(extractMessageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromMessageXPathDefaultNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        receiveAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/:TestRequest/:Message", "messageVar");
        receiveAction.setExtractMessageElements(extractMessageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        receiveAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns0:Message", "messageVar");
        receiveAction.setExtractMessageElements(extractMessageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNestedNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        receiveAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\" xmlns:ns1=\"http://citrusframework.org/unittest/message\">" +
                "<ns1:Message>Hello World!</ns1:Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns1:Message", "messageVar");
        receiveAction.setExtractMessageElements(extractMessageElements);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceBindings() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setSchemaValidation(false);
        receiveAction.setMessageData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        validator.setFunctionRegistry(context.getFunctionRegistry());
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/pfx:TestRequest/pfx:Message", "messageVar");
        receiveAction.setExtractMessageElements(extractMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        receiveAction.setNamespaces(namespaces);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveMessageWithTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(5000L);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive(5000L)).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveSelectedWithMessageSelectorString() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        String messageSelectorString = "Operation = 'sayHello'";
        receiveAction.setMessageSelectorString(messageSelectorString);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receiveSelected(messageSelectorString)).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveSelectedWithMessageSelectorStringAndTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(5000L);
        
        String messageSelectorString = "Operation = 'sayHello'";
        receiveAction.setMessageSelectorString(messageSelectorString);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receiveSelected(messageSelectorString, 5000L)).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveSelectedWithMessageSelectorMap() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, String> messageSelector = new HashMap<String, String>();
        messageSelector.put("Operation", "sayHello");
        receiveAction.setMessageSelector(messageSelector);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receiveSelected("Operation = 'sayHello'")).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveSelectedWithMessageSelectorMapAndTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(5000L);
        
        Map<String, String> messageSelector = new HashMap<String, String>();
        messageSelector.put("Operation", "sayHello");
        receiveAction.setMessageSelector(messageSelector);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receiveSelected("Operation = 'sayHello'", 5000L)).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    public void testMessageTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(null).once();
        replay(messageReceiver);
        
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Received message is null!");
            verify(messageReceiver);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving no message");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveEmptyMessagePayloadAsExpected() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        receiveAction.execute(context);
        
        verify(messageReceiver);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveEmptyMessagePayloadUnexpected() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageReceiver(messageReceiver);
        receiveAction.setValidator(validator);
        receiveAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("")
                                    .copyHeaders(headers)
                                    .build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive()).andReturn(controlMessage).once();
        replay(messageReceiver);
        
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Validation error: Received message body is empty");
            verify(messageReceiver);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving unexpected empty message payload");
    }
    
    @Test
    @SuppressWarnings("unchecked")
	public void testReceiveMessageWithValidationScript() {
		ReceiveMessageAction receiveAction = new ReceiveMessageAction();
		receiveAction.setMessageReceiver(messageReceiver);
		receiveAction.setValidator(validator);
		receiveAction.setValidationScript("assert root.Message.name() == 'Message'\n" + "assert root.Message.text() == 'Hello World!'");
		receiveAction.setValidationScriptResource(new ClassPathResource("validation-script.groovy", SendMessageActionTest.class));
		
		Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
		reset(messageReceiver);
		expect(messageReceiver.receive()).andReturn(controlMessage).once();
		replay(messageReceiver);
		
		receiveAction.execute(context);
		
		verify(messageReceiver);
	}
    
    @Test
    @SuppressWarnings("unchecked")
	public void testReceiveMessageWithValidationScriptResource() {
		ReceiveMessageAction receiveAction = new ReceiveMessageAction();
		receiveAction.setMessageReceiver(messageReceiver);
		receiveAction.setValidator(validator);
		receiveAction.setValidationScriptResource(new ClassPathResource("validation-script.groovy", SendMessageActionTest.class));
		
		Map<String, Object> headers = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(headers)
                                    .build();
        
		reset(messageReceiver);
		expect(messageReceiver.receive()).andReturn(controlMessage).once();
		replay(messageReceiver);
		
		receiveAction.execute(context);
		
		verify(messageReceiver);
	}
}
