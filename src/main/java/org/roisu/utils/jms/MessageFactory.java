package org.roisu.utils.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

public interface MessageFactory
{
    /**
     * Create a javax.jms.BytesMessage
     */
    public BytesMessage createBytesMessage() throws JMSException;

    /**
     * Create a javax.jms.MapMessage
     */
    public MapMessage createMapMessage() throws JMSException;

    /**
     * Create a javax.jms.ObjectMessage
     */
    public ObjectMessage createObjectMessage() throws JMSException;

    /**
     * Create a javax.jms.ObjectMessage
     */
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException;

    /**
     * Create a javax.jms.StreamMessage
     */
    public StreamMessage createStreamMessage() throws JMSException;

    /**
     * Create a javax.jms.TextMessage
     */
    public TextMessage createTextMessage() throws JMSException;

    /**
     * Create a javax.jms.BytesMessage
     */
    public TextMessage createTextMessage(String text) throws JMSException;
    
    /**
     * Create a weblogic.jms.extensions.XMLMessage
     */
    public TextMessage createWLXMLMessage() throws JMSException;
    
    /**
     * Create a weblogic.jms.extensions.XMLMessage
     */
    public TextMessage createWLXMLMessage(String text) throws JMSException;

}
