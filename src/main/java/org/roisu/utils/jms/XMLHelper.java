package org.roisu.utils.jms;

import java.io.InputStream;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;

import org.roisu.utils.jms.xml.JMSMessage;
import org.roisu.utils.jms.xml.MessageSet;

/**
 * The Interface XMLHelper.
 * 
 * @author marcos.lois
 */
public interface XMLHelper
{
   
   /**
    * From xml.
    *
    * @param messageFactory the message factory
    * @param istream the istream
    * @return the collection
    * @throws JMSException the jMS exception
    */
   public Collection<Message> fromXML(MessageFactory messageFactory, InputStream istream) throws JMSException;

   /**
    * From xml.
    *
    * @param messageFactory the message factory
    * @param document the document
    * @return the collection
    * @throws JMSException the jMS exception
    */
   public Collection<Message> fromXML(MessageFactory messageFactory, String document) throws JMSException;
   
   /**
    * From message set.
    *
    * @param messageFactory the message factory
    * @param messageSet the message set
    * @return the collection
    * @throws JMSException the jMS exception
    */
   public Collection<Message> fromMessageSet(MessageFactory messageFactory, MessageSet messageSet) throws JMSException;
   
   /**
    * Creates the message.
    *
    * @param messageFactory the message factory
    * @param message the message
    * @return the message
    */
   public Message createMessage(MessageFactory messageFactory, JMSMessage message) throws JMSException;

   /**
    * Creates a MessageSet from xml.
    *
    * @param istream the istream
    * @return the message set
    */
   public MessageSet fromXML(InputStream istream);

   /**
    * Creates a MessageSet from xml.
    *
    * @param document the document
    * @return the message set
    */
   public MessageSet fromXML(String document);
}
