package com.lifotech.aws.ses;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;

public class AWSEmailService {

	private static Logger logger = Logger.getLogger(AWSEmailService.class);

	/*
	 * Important: Be sure to fill in an email address you have access to so that
	 * you can receive the initial confirmation email from Amazon Simple Email
	 * Service.
	 */
	private static final String TO = "skcybertech@gmail.com";
	private static final String FROM = "sksingh.psuh@gmail.com";
	private static final String BODY = "Hello World!";
	private static final String SUBJECT = "Hello World!";

	private static AmazonSimpleEmailServiceClient amazonSimpleEmailServiceClient;

	public static void init() {
		logger.debug("starting init()");
		AWSCredentialsProvider awsCredentialsProvider = getAWSCredentialsProvoider();
		amazonSimpleEmailServiceClient = new AmazonSimpleEmailServiceClient(awsCredentialsProvider);
		logger.debug("returning from init()");
	}

	public static void sendEmail() {
		init();

		/*
		 * Before you can send email via Amazon SES, you need to verify that you
		 * own the email address from which you???ll be sending email. This will
		 * trigger a verification email, which will contain a link that you can
		 * click on to complete the verification process.
		 */
		verifyEmailAddress(amazonSimpleEmailServiceClient, FROM);

		/*
		 * If you've just signed up for SES, then you'll be placed in the Amazon
		 * SES sandbox, where you must also verify the email addresses you want
		 * to send mail to.
		 * 
		 * You can uncomment the line below to verify the TO address in this
		 * sample.
		 * 
		 * Once you have full access to Amazon SES, you will *not* be required
		 * to verify each email address you want to send mail to.
		 * 
		 * You can request full access to Amazon SES here:
		 * http://aws.amazon.com/ses/fullaccessrequest
		 */
		// verifyEmailAddress(ses, TO);

		Session session = getSession();

		try {
			// Create a new Message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(FROM));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
			msg.setSubject(SUBJECT);
			msg.setText(BODY);
			msg.saveChanges();

			// Reuse one Transport object for sending all your messages
			// for better performance
			Transport t = new AWSJavaMailTransport(session, null);
			t.connect();
			t.sendMessage(msg, null);

			// Close your transport when you're completely done sending
			// all your messages
			t.close();
		} catch (AddressException e) {
			e.printStackTrace();
			System.out.println("Caught an AddressException, which means one or more of your " + "addresses are improperly formatted.");
		} catch (MessagingException e) {
			e.printStackTrace();
			System.out.println("Caught a MessagingException, which means that there was a " + "problem sending your message to Amazon's E-mail Service check the "
					+ "stack trace for more information.");
		}

	}

	private static void verifyEmailAddress(AmazonSimpleEmailService ses, String address) {
		ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
		if (verifiedEmails.getVerifiedEmailAddresses().contains(address))
			return;

		ses.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(address));
		System.out.println("Please check the email address " + address + " to verify it");
		System.exit(0);
	}

	private static AWSCredentialsProvider getAWSCredentialsProvoider() {
		return new ClasspathPropertiesFileCredentialsProvider();
	}

	private static Session getSession() {

		/*
		 * Setup JavaMail to use the Amazon Simple Email Service by specifying
		 * the "aws" protocol.
		 */
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "aws");

		/*
		 * Setting mail.aws.user and mail.aws.password are optional. Setting
		 * these will allow you to send mail using the static transport send()
		 * convince method. It will also allow you to call connect() with no
		 * parameters. Otherwise, a user name and password must be specified in
		 * connect.
		 */
		AWSCredentialsProvider awsCredentialsProvider = getAWSCredentialsProvoider();
		props.setProperty("mail.aws.user", awsCredentialsProvider.getCredentials().getAWSAccessKeyId());
		props.setProperty("mail.aws.password", awsCredentialsProvider.getCredentials().getAWSSecretKey());

		return Session.getInstance(props);

	}
}
