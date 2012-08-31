package org.roisu.utils.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import weblogic.jms.extensions.WLSession;

public class DefaultMessageFactory implements MessageFactory {
	private Session session;
	
	public DefaultMessageFactory(Session session) {
		this.session = session;
	}

	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		return session.createBytesMessage();
	}

	@Override
	public MapMessage createMapMessage() throws JMSException {
		return session.createMapMessage();
	}

	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		return session.createObjectMessage();
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
		return session.createObjectMessage(object);
	}

	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		return session.createStreamMessage();
	}

	@Override
	public TextMessage createTextMessage() throws JMSException {
		return session.createTextMessage();
	}

	@Override
	public TextMessage createTextMessage(String text) throws JMSException {
		return session.createTextMessage(text);
	}

	@Override
	public TextMessage createWLXMLMessage() throws JMSException {
		return ((WLSession) session).createXMLMessage();
	}

	@Override
	public TextMessage createWLXMLMessage(String xml) throws JMSException {
		return ((WLSession) session).createXMLMessage(xml);
	}

}
