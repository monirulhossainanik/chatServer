import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class Server extends JFrame implements Runnable {

    ServerSocket serverSocket;
    InetAddress address = InetAddress.getByName("192.168.42.28");
    static Vector<Socket> ClientSockets;
    static Vector<String> LoginNames;
    private JFrame f;
    static JTextField tf1, tf2;
    static JTextArea jta, cl;
    private JPanel p1, p2;

    Server() throws Exception {

        f = new JFrame("Chat Server");
        tf1 = new JTextField(24);
        tf2 = new JTextField(12);
        jta = new JTextArea(70, 70);
        cl = new JTextArea(30, 20);
        p1 = new JPanel();
        p2 = new JPanel();

        serverSocket = new ServerSocket(65535, 1, address);
        ClientSockets = new Vector();
        LoginNames = new Vector();
        frameSetup();

    }

    void frameSetup() {
        Font font = new Font("Monospaced", Font.BOLD, 14);
        jta.setFont(font);
        cl.setFont(font);
        tf1.setFont(font);
        tf2.setFont(font);
        cl.setText("Current User:");
        cl.enable(false);
        jta.enable(false);
        tf1.enable(false);
        tf2.enable(false);
        tf1.setText("IP: " + serverSocket.getInetAddress());
        tf2.setText("Port: " + serverSocket.getLocalPort());
        p1.setBackground(Color.WHITE);
        p2.setBackground(Color.WHITE);
        p1.setSize(500, 200);
        p2.setSize(500, 300);
        p1.setLayout(new GridLayout(1, 3));
        p1.add(tf1);
        p1.add(tf2);
        p1.add(cl);
        p2.add(jta);
        f.add(p1);
        f.add(p2);
        f.setSize(500, 500);
        f.setLayout(new GridLayout(2, 1));
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {

        try {
            Server s = new Server();
            Thread t = new Thread(s);
            t.start();
        } catch (Exception e) {
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket server = serverSocket.accept();
                AcceptClient connectedClient = new AcceptClient(server);
            } catch (Exception e) {
            	e.printStackTrace();
            }

        }
    }

    class AcceptClient extends Thread {

        Socket ClientSocket;
        DataInputStream din;
        DataOutputStream dout;

        AcceptClient(Socket connectedSoc) throws Exception {
            ClientSocket = connectedSoc;

            din = new DataInputStream(ClientSocket.getInputStream());
            dout = new DataOutputStream(ClientSocket.getOutputStream());

            String LoginName = din.readUTF();
            jta.append("\nUser Logged In :" + LoginName);
            LoginNames.add(LoginName);
            ClientSockets.add(ClientSocket);
            cl.setText("Current User:");
            for (int i = 0; i < LoginNames.size(); i++) {
                cl.append("\n" + LoginNames.elementAt(i).toString());
            }
            String extra = "USER ";
            for (int i = 0; i < LoginNames.size(); i++) {
                extra = extra + LoginNames.elementAt(i).toString() + ",";
            }
            for (int i = 0; i < LoginNames.size(); i++) {
                try {
                    Socket soc = (Socket) ClientSockets.elementAt(i);
                    DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
                    dout.writeUTF(extra);
                } catch (Exception e) {
                }
            }
            start();
        }

        public void run() {
            while (true) {

                try {
                    String msgFromClient = new String();
                    msgFromClient = din.readUTF();
                    String[] message = msgFromClient.split("~");

                    String Sendto = message[0];
                    String MsgType = message[1];
                    int i;

                    if (MsgType.equals("LOGOUT")) {
                        for (i = 0; i < LoginNames.size(); i++) {
                            if (LoginNames.elementAt(i).equals(Sendto)) {
                                Socket soc = (Socket) ClientSockets.elementAt(i);
                                soc.close();
                                LoginNames.removeElementAt(i);
                                ClientSockets.removeElementAt(i);
                                jta.append("\nUser " + Sendto + " Logged Out ...");

                                cl.setText("Current User:");
                                for (i = 0; i < LoginNames.size(); i++) {
                                    cl.append("\n" + LoginNames.elementAt(i).toString());
                                }
                                break;
                            }
                        }
                        String extra = "USER ";
                        for (i = 0; i < LoginNames.size(); i++) {
                            extra = extra + LoginNames.elementAt(i).toString() + ",";
                        }
                        for (i = 0; i < LoginNames.size(); i++) {
                            try {
                                Socket soc = (Socket) ClientSockets.elementAt(i);
                                DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
                                dout.writeUTF(extra);
                            } catch (Exception e) {
                            }
                        }

                    } else {
                        String msg = "DATA ";

                        msg = msg + message[2];

                        for (i = 0; i < LoginNames.size(); i++) {
                            if (LoginNames.elementAt(i).equals(Sendto)) {
                                Socket soc = (Socket) ClientSockets.elementAt(i);
                                DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
                                dout.writeUTF(msg);
                                break;
                            }
                        }

                        if (i == LoginNames.size()) {
                            msg = "DATA " + Sendto + " -> offline";
                            dout.writeUTF(msg);
                        }
                    }
                    if (MsgType.equals("LOGOUT")) {
                        break;
                    }

                } catch (Exception ex) {
                }

            }
        }
    }
}
