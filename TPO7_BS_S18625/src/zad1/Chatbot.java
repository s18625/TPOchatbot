package zad1;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.naming.InitialContext;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.TopicSession;
import javax.jms.JMSException;
import javax.jms.TopicPublisher;
import javax.jms.MessageListener;
import javax.jms.TopicSubscriber;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;


public class Chatbot extends JFrame {

    private TopicSubscriber subscriber;
    private JTextArea publicChat = new JTextArea();
    private JTextArea privateChat = new JTextArea();
    private JButton send = new JButton("Send");
    private JButton clear = new JButton("Clear");
    private String sender;


    public static void main(String[] args) throws Exception {
        Chatbot chat = new Chatbot(args[0]);
    }

    public Chatbot(String sender) throws Exception {
        this.sender = sender;
        setTitle("Witaj w czacie: " + sender);
        setLocation(600, 200);
        publicChat.setBackground(Color.GRAY);


        InitialContext ctx = new InitialContext();
        TopicConnectionFactory factory = (TopicConnectionFactory)
                ctx.lookup("ConnectionFactory");
        final Topic topic = (Topic) ctx.lookup("topic1");
        TopicConnection conn = factory.createTopicConnection();
        final TopicSession session = conn.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);
        final TopicPublisher publisher = session.createPublisher(topic);
        subscriber = session.createSubscriber(topic);
        publicChat.setEditable(false);


        // menu.add(clear);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(privateChat);
        panel.add(send, BorderLayout.EAST);
        panel.add(clear, BorderLayout.WEST);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                publicChat, panel);
        split.setOneTouchExpandable(true);
        split.setDividerLocation(205);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split);

        setSize(new Dimension(400, 300));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                //Object src = ev.getSource();
                //if (src == clear)
                publicChat.setText("");
            }
        });

        setMessageListener();
        conn.start();

        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = privateChat.getText();
                if (msg.length() > 0) {
                    try {
                        //publicChat.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                        TextMessage m = session.createTextMessage(sender + ": " + msg);
                        publisher.publish(m);
                        privateChat.setText("");
                    } catch (JMSException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        privateChat.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    send.doClick();
                }
            }
            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });


    }

    int i =0;
    public void setMessageListener() throws JMSException {
        subscriber.setMessageListener(new MessageListener() {
            public void onMessage(Message m) {
                try {

                    String strings[] =  m.toString().split(":");
                    System.out.println(Arrays.toString(strings));


                    if (!strings[0].equals(sender)){
                        System.out.println(strings[0]+"--"+sender);
                        publicChat.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                        publicChat.setBackground(Color.RED);
                    }
                    else {
                        publicChat.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                        publicChat.setBackground(Color.GRAY);

                    }

                    setTitle(sender+"Chat"+" - "+ "Received msg " + ++i+" from: "+strings[0]);
                    TextMessage msg = (TextMessage) m;
                    publicChat.append("-" + msg.getText() + "\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


}