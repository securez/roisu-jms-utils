package org.roisu.utils.jms;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.roisu.utils.jms.xml.JMSMessage;
import org.roisu.utils.jms.xml.MessageSet;

public class QueuePublisher {	
	private QueueConnectionFactory connectionFactory;
	private QueueConnection connection;
	private QueueSession session;
	private QueueSender sender;
	private Queue queue;
	private MessageFactory messageFactory;
	private XMLHelper xmlHelper = new DefaultXMLHelper();
	
	public QueuePublisher(String connectionFactory, String destination, Properties initialContextProperties) throws NamingException, JMSException  {
		Context ctx = null;

		try {
			ctx = new InitialContext(initialContextProperties);
	        
	        this.connectionFactory = (QueueConnectionFactory) ctx.lookup(connectionFactory);
			connection = this.connectionFactory.createQueueConnection();
		    session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		    queue = (Queue) ctx.lookup(destination);
		    sender = session.createSender(queue);
		    messageFactory = new DefaultMessageFactory(session);
        } catch (NamingException e) {
	        throw e;
        } finally {
        	//ctx.close();
        }
	}
	
	public Message publish(String text) throws JMSException {
		Message msg = messageFactory.createTextMessage(text);
		
		sender.send(msg);
		
		return msg;
	}
	
	public Message publish(JMSMessage message) throws JMSException {
		Message msg = xmlHelper.createMessage(messageFactory, message);
		
		if(message.getJMSPriority() != null) {
			sender.setPriority(message.getJMSPriority());
		}
		sender.send(msg);
		
		return msg;
	}
	
	public Collection<Message> publish(MessageSet messages) throws JMSException {
		Collection<Message> msgs = xmlHelper.fromMessageSet(messageFactory, messages);
		
		for(Message message : msgs) {
			// TODO : Handle priority...
			sender.setPriority(4);
			sender.send(message);
		}
		
		return msgs;
	}
	
	public Collection<Message> publishXML(InputStream istream) throws JMSException {
		Collection<Message> msgs = xmlHelper.fromXML(messageFactory, istream);
		
		for(Message msg : msgs) {
			sender.send(msg);
		}
		
		return msgs;
	}
	
	public Collection<Message> publishXML(String document) throws JMSException {
		Collection<Message> msgs = xmlHelper.fromXML(messageFactory, document);
		
		for(Message msg : msgs) {
			sender.send(msg);
		}
		
		return msgs;
	}
	
	public void publish(Message message) throws JMSException {
		sender.send(message);
	}

	public QueueSession getSession() {
    	return session;
    }

	public void setSession(QueueSession session) {
    	this.session = session;
    }

	public QueueSender getSender() {
    	return sender;
    }

	public void setSender(QueueSender sender) {
    	this.sender = sender;
    }

	public Queue getQueue() {
    	return queue;
    }

	public void setQueue(Queue queue) {
    	this.queue = queue;
    }

	public MessageFactory getMessageFactory() {
    	return messageFactory;
    }

	public QueueConnectionFactory getConnectionFactory() {
    	return connectionFactory;
    }

	public QueueConnection getConnection() {
    	return connection;
    }

	public XMLHelper getXmlHelper() {
    	return xmlHelper;
    }

}
