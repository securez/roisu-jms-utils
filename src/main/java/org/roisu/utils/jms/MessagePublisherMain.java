package org.roisu.utils.jms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

// TODO : Implement CLI
/**
 * The Class MessagePublisherMain.
 */
public class MessagePublisherMain {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws NamingException the naming exception
	 * @throws JMSException the jMS exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public static void main(String[] args) throws NamingException, JMSException, FileNotFoundException {
		Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        env.put(Context.PROVIDER_URL, "t3://localhost:7001");
        env.put(Context.SECURITY_PRINCIPAL, "weblogic");
        env.put(Context.SECURITY_CREDENTIALS, "weblogic0");
		
		
		QueuePublisher qp = new QueuePublisher("jms/TestDevQueueFactory", "jms/TestDevQueue", env);
		InputStream in = new FileInputStream(new File("xml/full/jms2xml.xml"));
			

		qp.publishXML(in);

	}

}
