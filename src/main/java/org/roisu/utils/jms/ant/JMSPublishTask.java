package org.roisu.utils.jms.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.roisu.utils.jms.BlockThenRunPolicy;
import org.roisu.utils.jms.QueuePublisher;
import org.roisu.utils.jms.xml.JMSMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class WeblogicPublishJMSTask is a Ant task utility to publish jms messages
 * with Weblogic extensions.
 * 
 * @author marcos.lois
 */
public class JMSPublishTask extends Task {
	
	/** The initial context factory. */
	private String initialContextFactory;
	
	/** The provider url. */
	private String providerURL;
	
	/** The principal. */
	private String principal;
	
	/** The credential. */
	private String credential;
	
	/** The jms connection factory. */
	private String jmsConnectionFactory;
	
	/** The jms destination. */
	private String jmsDestination;
	
	/** The file charset. */
	private String fileCharset = "UTF-8";
	
	/** The number of threads that will publish messages. */
	private int numThreads = 1;
	
	/** The filesets. */
	private List<FileSet> filesets = new ArrayList<FileSet>();
	
	/** The time to live */
	private Long timeToLive;
	
	/**
	 * Validate passed parameters.
	 */
	public void validate() {
		// TODO : Add basic validation of parameters
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	/* (non-Javadoc)
     * @see org.roisu.utils.jms.ant.IJMSPublishTask#execute()
     */
	@Override
    public void execute() throws BuildException {
		validate();
        
        Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		props.put(Context.PROVIDER_URL, providerURL);
		props.put(Context.SECURITY_PRINCIPAL, principal);
		props.put(Context.SECURITY_CREDENTIALS, credential);
		
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		ThreadPoolExecutor threadPool = null;
		
		try {
	        QueuePublisher publisher = new QueuePublisher(jmsConnectionFactory, jmsDestination, props);
	        if(this.timeToLive != null) {
	        	publisher.getSender().setTimeToLive(this.timeToLive.longValue());
	        }
	       
	        if(numThreads > 1) {
	        	log("Starting pool with " + numThreads + " threads ...");
	        	throw new BuildException("Threaded publisher not implemented.");
	        	/*
	        	BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(numThreads * 2);
	        	threadPool = new  ThreadPoolExecutor(numThreads, numThreads, Integer.MAX_VALUE, TimeUnit.SECONDS, workQueue, new BlockThenRunPolicy(Long.MAX_VALUE, TimeUnit.SECONDS));
	        	*/
	        } else {
				for (File file :  getFilesToSend()) {
					log("Publish XML file " + file.getAbsolutePath());
					for(JMSMessage message : publisher.getXmlHelper().fromXML(new FileInputStream(file)).getMessages()) {
						log("Publish message [ID = " + message.getJMSMessageID() + "]");
						publisher.publish(message);
					}
					
				}
	        }
        } catch (Exception e) {
        	throw new BuildException(e);
        } finally {
        	Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
	
	
	/**
	 * Gets the files to send.
	 *
	 * @return the files to send
	 */
	private List<File> getFilesToSend() {
		List<File> files = new ArrayList<File>();
		
		// Iterate over one or more <fileset> elements
		for (FileSet fs :  filesets) {
			// Refer the result of fileset via the directory scanner
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			for (String file : includedFiles) {
				String filename = file.replace('\\', '/');
				filename = filename.substring(filename.lastIndexOf("/") + 1);
				
				// Get absolute path
				files.add(new File(ds.getBasedir(),file));

			}
		}
		
		return files;
	}
	
	/**
	 * Read file.
	 *
	 * @param file the file
	 * @param csName the cs name
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readFile(String file, String csName) throws IOException {
		Charset cs = Charset.forName(csName);
		return readFile(file, cs);
	}

	/**
	 * Read file.
	 *
	 * @param file the file
	 * @param cs the cs
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readFile(String file, Charset cs) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				log("Error closing stram : " + e.getMessage(), e, Project.MSG_WARN);
			}
		}
	}
	
    /**
	 * Adds the file set.
	 *
	 * @param fileset the fileset
	 */
	public void addFileSet(FileSet fileset) {
		filesets.add(fileset);
	}

    /**
	 * Sets the initial context factory.
	 *
	 * @param initialContextFactory the new initial context factory
	 */
	public void setInitialContextFactory(String initialContextFactory) {
    	this.initialContextFactory = initialContextFactory;
    }

    /**
	 * Sets the provider url.
	 *
	 * @param providerURL the new provider url
	 */
	public void setProviderURL(String providerURL) {
    	this.providerURL = providerURL;
    }

    /**
	 * Sets the principal.
	 *
	 * @param principal the new principal
	 */
	public void setPrincipal(String principal) {
    	this.principal = principal;
    }

    /**
	 * Sets the credential.
	 *
	 * @param credential the new credential
	 */
	public void setCredential(String credential) {
    	this.credential = credential;
    }

    /**
	 * Sets the jms connection factory.
	 *
	 * @param jmsConnectionFactory the new jms connection factory
	 */
	public void setJmsConnectionFactory(String jmsConnectionFactory) {
    	this.jmsConnectionFactory = jmsConnectionFactory;
    }

    /**
	 * Sets the jms destination.
	 *
	 * @param jmsDestination the new jms destination
	 */
	public void setJmsDestination(String jmsDestination) {
    	this.jmsDestination = jmsDestination;
    }

    /**
	 * Sets the file charset.
	 *
	 * @param fileCharset the new file charset
	 */
	public void setFileCharset(String fileCharset) {
    	this.fileCharset = fileCharset;
    }
	
    /**
	 * Sets the num threads.
	 *
	 * @param numThreads the new num threads
	 */
	public void setNumThreads(int  numThreads) {
    	this.numThreads = numThreads;
    }
	
	
	public void setTimeToLive(long timeToLive) {
    	this.timeToLive = new Long(timeToLive);
    }

}
