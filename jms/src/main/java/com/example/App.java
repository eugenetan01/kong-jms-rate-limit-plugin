package com.example;
import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import com.example.App.HelloWorldConsumer;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.jms.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        int numConsumers = 5; // Number of consumer threads
        /*ExecutorService executorServiceProd = Executors.newFixedThreadPool(numConsumers); */
        HelloWorldProducer producer = new HelloWorldProducer();
        /* for (int i = 0; i < numConsumers; i++) {
            executorServiceProd.submit(new HelloWorldProducer());
        } */

        
        producer.run();
        /* executorServiceProd.shutdown();
        executorServiceProd.awaitTermination(1, TimeUnit.HOURS); */

        //HelloWorldConsumer consumer = new HelloWorldConsumer();

        ExecutorService executorService = Executors.newFixedThreadPool(numConsumers);

        for (int i = 0; i < numConsumers; i++) {
            executorService.submit(new HelloWorldConsumer());
        }

        //producer.run();

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        //HelloWorldConsumer consumer = new HelloWorldConsumer();
        
        //consumer.run();

    }

    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                // This is connecting to local Kong instance - proxying TCP traffic via port 9000. Do ensure service and route is created first.
                TibjmsQueueConnectionFactory connectionFactory = new TibjmsQueueConnectionFactory("tcp://localhost:9000");
                // Create a connection
                Connection connection = connectionFactory.createConnection("admin", "");
                // Create a Connection
                //connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("kong.test");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                String requestID = "123";
                String timestamp = "2024-07-05T10:15:30Z";
                String plainMessage = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ns=\"http://www.bidv.com.vn/entity/global/vn/account/acctdetail/fdrdetailinq/1.0\" xmlns:ns1=\"http://www.bidv.com/common/envelope/commonheader/1.0\" xmlns:ns2=\"http://www.bidv.com/global/common/account/1.0\"><soap:Header/><soap:Body><ns:FDRDetailInqReq><ns1:Header><ns1:Common><ns1:BusinessDomain>CBS.TEST.BIDV.COM.VN</ns1:BusinessDomain><ns1:ServiceVersion>1.3</ns1:ServiceVersion><ns1:MessageId>" + requestID + "</ns1:MessageId><ns1:MessageTimestamp>" + timestamp + "</ns1:MessageTimestamp></ns1:Common><ns1:Client><ns1:SourceAppID>OMNI</ns1:SourceAppID></ns1:Client></ns1:Header><ns:BodyReqFDRDetailInquiry><ns:MoreRecordIndicator></ns:MoreRecordIndicator><ns:AccInfoType><ns2:AcctNo>2223619077</ns2:AcctNo><ns2:AcctType>CD</ns2:AcctType><ns2:CurCode>VND</ns2:CurCode></ns:AccInfoType></ns:BodyReqFDRDetailInquiry></ns:FDRDetailInqReq></soap:Body></soap:Envelope>";


                TextMessage message = session.createTextMessage(plainMessage);
                for (int i = 0; i < 10; i++){
                    producer.send(message);
                    System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
                }
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        
    }

    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                TibjmsQueueConnectionFactory connectionFactory = new TibjmsQueueConnectionFactory("tcp://<tibco-ems-server>:<port>");
                // Create a connection
                Connection connection = connectionFactory.createConnection("admin", "");
                // Create a Connection
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("kong.test");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);

                int counter = 0;
                while (true) {
                    Message message = consumer.receive(5000);  // Wait up to 5 seconds for a message
                    if (message != null) {
                        System.out.println("Message received");
                        if (message instanceof BytesMessage) {
                            BytesMessage bytesMessage = (BytesMessage) message;
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = bytesMessage.readBytes(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
    
                            String messageContent = outputStream.toString("UTF-8");
                            System.out.println("Received BytesMessage content: " + messageContent);
    
                            outputStream.close();
                        } else if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            System.out.println("Received TextMessage content: " + textMessage.getText());
                            counter += 1;
                        } else {
                            System.out.println("Received non-bytes message: " + message);
                        }
                    } else {
                        System.out.println("total num messages: " + counter);
                        System.out.println("No more messages.");
                        break;
                    }
                } 
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            } 
        }

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}