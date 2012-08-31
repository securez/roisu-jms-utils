package org.roisu.utils.jms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.util.ReaderInputStream;
import org.roisu.utils.jms.xml.JMSBytesMessage;
import org.roisu.utils.jms.xml.JMSMapMessage;
import org.roisu.utils.jms.xml.JMSMessage;
import org.roisu.utils.jms.xml.JMSObjectMessage;
import org.roisu.utils.jms.xml.JMSTextMessage;
import org.roisu.utils.jms.xml.MessageSet;
import org.roisu.utils.jms.xml.Property;

public class DefaultXMLHelper implements XMLHelper {
	private static JAXBContext jxbCtx;
	private static final String BASE64_CODEC = "Base64";

	/*
	static {
		try {
	        jxbCtx = JAXBContext.newInstance(JMSMessage.class.getPackage().getName());
        } catch (JAXBException e) {
	        throw new DataBindingException(e);
        }
	}
	*/
	
	public DefaultXMLHelper() {
		synchronized(DefaultXMLHelper.class) {
			if(jxbCtx == null) {
				try {
			        jxbCtx = JAXBContext.newInstance(JMSMessage.class.getPackage().getName());
		        } catch (JAXBException e) {
			        throw new DataBindingException(e);
		        }
			}
		}
	}

	private final ThreadLocal<Base64> base64EncoderTL = new ThreadLocal<Base64>() {
		@Override
		protected Base64 initialValue() {
			return new Base64();
		}
	};
	 
	
	private final ThreadLocal<Unmarshaller> jxbUnmarshaller = new ThreadLocal<Unmarshaller>() {
		@Override
		protected Unmarshaller initialValue() {
			try {
				return jxbCtx.createUnmarshaller();
			} catch (JAXBException e) {
				// TODO : Add log
				//log.error(e.getMessage(), e);
				return null;
			}
		}
	};
	
	
	public MessageSet readContent(InputStream istream) throws Exception {
		Unmarshaller u = jxbUnmarshaller.get();
		MessageSet node = (MessageSet) u.unmarshal(new StreamSource(istream));

		return node;
	}

	public MessageSet readContent(Reader reader) throws JAXBException   {
		Unmarshaller u = jxbUnmarshaller.get();
		MessageSet node = (MessageSet) u.unmarshal(new StreamSource(new ReaderInputStream(reader)));

		return node;
	}

	@Override
	public Collection<Message> fromXML(MessageFactory messageFactory, InputStream istream) throws JMSException {
			try {
	            return fromMessageSet(messageFactory,  readContent(istream));
            } catch (Exception e) {
	            throw new DataBindingException(e);
            }
	}

	@Override
	public Collection<Message> fromXML(MessageFactory messageFactory, String document) throws JMSException {
		try {
	        return fromMessageSet(messageFactory, readContent(new StringReader(document)));
        } catch (JAXBException e) {
        	throw new DataBindingException(e);
        }
	}
	
	@Override
	public MessageSet fromXML(InputStream istream) {
			try {
	            return readContent(istream);
            } catch (Exception e) {
	            throw new DataBindingException(e);
            }
	}

	@Override
	public MessageSet fromXML(String document) {
		try {
	        return readContent(new StringReader(document));
        } catch (JAXBException e) {
        	throw new DataBindingException(e);
        }
	}
	
	public Collection<Message> fromMessageSet(MessageFactory messageFactory, MessageSet messageSet) throws JMSException {
		Collection<Message> rval = new ArrayList<Message>();

		for (JMSMessage message : messageSet.getMessages()) {
			Message jmsMessage = null;

			jmsMessage = createMessage(messageFactory, message);

			if (jmsMessage != null) {
				rval.add(jmsMessage);
			}
		}

		return rval;
	}

	@Override
	public Message createMessage(MessageFactory messageFactory, JMSMessage message) throws JMSException {
		try {
			Message rval = null;

			if (message instanceof JMSTextMessage) {
				rval = messageFactory.createTextMessage();

				JMSTextMessage textMessage = (JMSTextMessage) message;
				TextMessage textRval = (TextMessage) rval;

				// TODO : Support other charsets in base64 encoding
				if (BASE64_CODEC.equals(textMessage.getCodec())) {
					byte[] bytes = base64EncoderTL.get().decode(textMessage.getText().getBytes());
					textRval.setText(new String(bytes, "ASCII"));
				} else {
					textRval.setText(textMessage.getText());
				}
			} else if (message instanceof JMSMapMessage) {
				rval = messageFactory.createMapMessage();

				JMSMapMessage mapMessage = (JMSMapMessage) message;
				MapMessage mapRval = (MapMessage) rval;

				for (Property property : mapMessage.getBodyProperty()) {
					if (property.getValue() == null) {
						mapRval.setObject(property.getName(), null);
					} else if (property.getType().equals(String.class.getName())) {
						mapRval.setString(property.getName(), property.getValue());
					} else if (property.getType().equals(Long.class.getName())) {
						mapRval.setLong(property.getName(), Long.parseLong(property.getValue()));
					} else if (property.getType().equals(Double.class.getName())) {
						mapRval.setDouble(property.getName(), Double.parseDouble(property.getValue()));
					} else if (property.getType().equals(Boolean.class.getName())) {
						mapRval.setBoolean(property.getName(), Boolean.getBoolean(property.getValue()));
					} else if (property.getType().equals(Character.class.getName())) {
						mapRval.setChar(property.getName(), property.getValue().charAt(0));
					} else if (property.getType().equals(Short.class.getName())) {
						mapRval.setShort(property.getName(), Short.parseShort(property.getValue()));
					} else if (property.getType().equals(Integer.class.getName())) {
						mapRval.setInt(property.getName(), Integer.parseInt(property.getValue()));
					}
				}
			} else if (message instanceof JMSBytesMessage) {
				rval = messageFactory.createBytesMessage();

				JMSBytesMessage bytesMessage = (JMSBytesMessage) message;
				BytesMessage bytesRval = (BytesMessage) rval;

				bytesRval.writeBytes(base64EncoderTL.get().decode(bytesMessage.getBytes().getBytes()));
			} else if (message instanceof JMSObjectMessage) {
				rval = messageFactory.createObjectMessage();

				JMSObjectMessage objectMessage = (JMSObjectMessage) message;
				ObjectMessage objectRval = (ObjectMessage) rval;
				ByteArrayInputStream bistream = new ByteArrayInputStream(base64EncoderTL.get().decode(objectMessage.getObject().getBytes()));

				ObjectInputStream oistream = new ObjectInputStream(bistream);

				objectRval.setObject((Serializable) oistream.readObject());
			}

			
			/* TODO : Revise support for JMSDeliveryMode, JMSMessageID
			// JMS Header properties
			try {
				rval.setJMSDeliveryMode(message.getJMSDeliveryMode());
			} catch (JMSException ex) {
				//log.error("unable to set JMSDeliveryMode to " + message.getJMSDeliveryMode() + ": " + ex.getMessage());
			}
			*/
			try {
				rval.setJMSMessageID(message.getJMSMessageID());
			} catch (JMSException ex) {
				//log.error("unable to set JMSMessageID: " + ex.getMessage(), ex);
			}
			
			try {
				if (message.getJMSExpiration() != null) {
					rval.setJMSExpiration(message.getJMSExpiration());
				}
			} catch (JMSException ex) {
				//log.error("unable to set JMSExpiration: " + ex.getMessage(), ex);
			}

			try {
				if (message.getJMSPriority() != null) {
					rval.setJMSPriority(message.getJMSPriority());
				}
			} catch (JMSException ex) {
				//log.error("unable to set JMSPriority: " + ex.getMessage(), ex);
			}

			try {
				if (message.getJMSTimestamp() != null) {
					rval.setJMSTimestamp(message.getJMSTimestamp());
				}
			} catch (JMSException ex) {
				//log.error("unable to set JMSTimestamp:" + ex.getMessage(), ex);
			}

			if (message.getJMSCorrelationID() != null) {
				rval.setJMSCorrelationID(message.getJMSCorrelationID());
			}
//	TODO : Add support to JMSReplyTo
//			if (message.getJMSReplyTo() != null && !message.getJMSReplyTo().equals("null")) {
//				rval.setJMSReplyTo(xxxx.getDestination(message.getJMSReplyTo(), ));
//			}

			if (message.getJMSType() != null) {
				rval.setJMSType(message.getJMSType());
			}
// TODO : Add support to JMSDestination
//			if (message.getJMSDestination() != null) {
//				if (message.isFromQueue()) {
//					rval.setJMSDestination(hermes.getDestination(message.getJMSDestination(), Domain.QUEUE));
//				} else {
//					rval.setJMSDestination(hermes.getDestination(message.getJMSDestination(), Domain.TOPIC));
//				}
//			}

			for (Property property : message.getHeaderProperty()) {
				if (property.getValue() == null) {
					rval.setObjectProperty(property.getName(), null);
				} else if (property.getType().equals(String.class.getName())) {
					rval.setStringProperty(property.getName(), StringEscapeUtils.unescapeXml(property.getValue()));
				} else if (property.getType().equals(Long.class.getName())) {
					rval.setLongProperty(property.getName(), Long.parseLong(property.getValue()));
				} else if (property.getType().equals(Double.class.getName())) {
					rval.setDoubleProperty(property.getName(), Double.parseDouble(property.getValue()));
				} else if (property.getType().equals(Boolean.class.getName())) {
					rval.setBooleanProperty(property.getName(), Boolean.parseBoolean(property.getValue()));
				} else if (property.getType().equals(Short.class.getName())) {
					rval.setShortProperty(property.getName(), Short.parseShort(property.getValue()));
				} else if (property.getType().equals(Integer.class.getName())) {
					rval.setIntProperty(property.getName(), Integer.parseInt(property.getValue()));
				}
			}

			return rval;
		} catch (JMSException e) {
			throw e;
        } catch (Exception e) {
        	throw new DataBindingException(e);
        } 
	}

}
