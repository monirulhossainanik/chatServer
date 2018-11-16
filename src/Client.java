import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Client extends JFrame implements Runnable, ActionListener, WindowListener {

    Socket soc;
    String LoginName;
    DataOutputStream dout;
    DataInputStream din;
    Thread t = null;
    static Client c = null;
    Vector current = null;
    private JFrame f;
    JButton send, close, ok;
    private JTextField jf1, jf2, jf3;
    private TextArea jta1, jta2;
    private JPanel p1, p2, p3, p4;

    private Client() {
        f = new JFrame("Client");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();
        p4 = new JPanel();
        send = new JButton("Send");
        close = new JButton("Close");
        ok = new JButton("Ok");
        ok.addActionListener(this);
        ok.setActionCommand("ok");
        jf1 = new JTextField(15);
        jf2 = new JTextField(15);
        jf3 = new JTextField(40);
        jta1 = new TextArea(50, 25);
        jta2 = new TextArea(50, 25);

    }

    void connectServer() {
        f.setTitle(LoginName);
        ok.removeActionListener(this);
        jf1.enable(false);
        jf2.enable(true);
        jf3.enable(true);
        jta1.enable(true);
        jta2.enable(true);
        send.addActionListener(this);
        close.addActionListener(this);
        send.setActionCommand("send");
        close.setActionCommand("close");

        try {
            soc = new Socket("192.168.42.28", 65535);

            din = new DataInputStream(soc.getInputStream());
            dout = new DataOutputStream(soc.getOutputStream());
            dout.writeUTF(LoginName);

        } catch (Exception e) {
        }

    }

    void frameSetup() {
        Font font = new Font("Monospaced", Font.BOLD, 14);
        jta1.setFont(font);
        jta2.setFont(font);
        jf1.setFont(font);
        jf2.setFont(font);
        jf3.setFont(font);
        jf2.enable(false);
        jf3.enable(false);

        jta1.setEditable(false);
        jta2.setEditable(false);
        p1.setBackground(Color.WHITE);
        p4.setBackground(Color.WHITE);
        p3.setLayout(new GridLayout(2, 2));
        JLabel l1 = new JLabel("User: ");
        p1.add(l1);
        p1.add(jf1);

        p1.add(ok);
        p2.add(jf3);
        p2.add(send);
        p2.add(close);
        JLabel l2 = new JLabel("Send to: ");
        p2.add(l2);
        p2.add(jf2);
        p3.setLayout(new GridLayout(2, 1));
        p3.add(p1);
        p3.add(p2);
        p4.setLayout(new FlowLayout());
        p4.add(jta1);
        p4.add(jta2);
        f.add(p3);
        f.add(p4);
        f.setSize(500, 500);
        f.setLayout(new GridLayout(2, 1));
        f.setVisible(true);
        f.addWindowListener(this);

    }

    @Override
    public void run() {
        while (true) {

            try {

                String msgFromServer;
                msgFromServer = din.readUTF();
                StringTokenizer st = new StringTokenizer(msgFromServer);
                String MsgType = st.nextToken();
                String str = "";
                while (st.hasMoreTokens()) {
                    str += st.nextToken() + " ";
                }

                if (MsgType.equals("USER")) {

                    String[] userList = str.split(",");
                    jta2.setText("Current User:");
                    for (String user : userList) {
                        jta2.append("\n" + user);
                    }
                } else {
                    jta1.append("\n" + str);
                }

            } catch (EOFException e) {
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().toString().equals("ok")) {
            if (!jf1.getText().equals("")) {
                c.LoginName = jf1.getText();
                connectServer();
                t = new Thread(c);
                t.start();
            }
        } else if (e.getActionCommand().toString().equals("send")) {
            if (!jf3.getText().equals("") && !jf2.getText().equals("")) {
                try {
                    dout.writeUTF(jf2.getText() + "~" + "DATA" + "~" + LoginName + " :" + jf3.getText());
                    jta1.append("\n" + LoginName + " :" + jf3.getText());
                    jf3.setText("");
                } catch (Exception ex) {
                    jta1.append("\nServer down or exception occurs");
                    jf3.setText("");
                }
            } else {
                jta1.append("\nMessage or Receiver field empty");
            }
        } else if (e.getActionCommand().toString().equals("close")) {
            try {
                dout.writeUTF(LoginName + "~" + "LOGOUT");

            } catch (Exception ex) {
            }
            System.exit(0);
        }
    }

    public void windowClosing(WindowEvent e) {
        try {
            dout.writeUTF(LoginName + "~" + "LOGOUT");

        } catch (Exception ex) {
        }
        System.exit(0);
    }

    public static void main(String args[]) throws Exception {
        c = new Client();

        c.frameSetup();

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
