/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author WinDows
 */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.*;

public class knowledgeGameClient extends JFrame implements ActionListener {

    //GUI elements textArea,frame,panel,JTextField,JLabel,JButton
    private JFrame frame = new JFrame("knowledgeGameClient");
    private JLabel messageLabel = new JLabel("");
    private JButton answer1;
    private JButton answer2;
    private JButton answer3;
    private JButton answer4;
    private JPanel panel2;
    private JPanel panel3;
    private JPanel intro;
    private JPanel panel;
    TextArea area = new TextArea(35, 70);
    Font f = new Font("Serif", Font.PLAIN, 15);
    String answer = "";
    private JLabel fixedTitle; //on the top of game
    private static int PORT = 8050; //prefixed in both side
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public knowledgeGameClient(String serverAddress) throws Exception {

        socket = new Socket(serverAddress, PORT); //initiate TCP connection to connect with server for playing the Knowledge Game quiz
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //used for receving information from server
        out = new PrintWriter(socket.getOutputStream(), true); //used for sending information from client to server
        //GUI element
        ImageIcon icon = new ImageIcon("icon4.jpg");
        frame.setIconImage(icon.getImage());
        fixedTitle = new JLabel("Welcome to online based quiz!");
        fixedTitle.setFont(f);
        fixedTitle.setForeground(Color.BLACK);
        answer1 = new JButton("-");
        answer2 = new JButton("-");
        answer3 = new JButton("-");
        answer4 = new JButton("-");
        panel2 = new JPanel();
        panel3 = new JPanel();
        intro = new JPanel();
        panel = new JPanel();
        panel.setSize(200, 200);
        intro.setBackground(Color.LIGHT_GRAY);
        panel.setBackground(Color.LIGHT_GRAY);
        panel.add(fixedTitle);
        frame.add(panel, BorderLayout.PAGE_START);
        area.setFont(f);
        panel2.setBackground(new Color(164, 209, 242));
        panel3.setBackground(new Color(164, 209, 242));
        panel3.setSize(500, 500);
        answer1.setAlignmentY(60);
        answer1.setBackground(Color.LIGHT_GRAY);
        answer2.setAlignmentY(60);
        answer2.setBackground(Color.LIGHT_GRAY);
        answer3.setAlignmentY(60);
        answer3.setBackground(Color.LIGHT_GRAY);
        answer4.setAlignmentY(60);
        answer4.setBackground(Color.LIGHT_GRAY);
        frame.add(panel2, BorderLayout.NORTH);
        messageLabel.setText("");
        messageLabel.setFont(f);
        messageLabel.setForeground(Color.BLACK);
        panel3.add(answer1);
        answer1.setPreferredSize(new Dimension(60, 60));
        panel3.add(answer2);
        answer2.setPreferredSize(new Dimension(60, 60));
        panel3.add(answer3);
        answer3.setPreferredSize(new Dimension(60, 60));
        panel3.add(answer4);
        answer4.setPreferredSize(new Dimension(60, 60));;

        frame.add(intro, BorderLayout.SOUTH);
        answer1.addActionListener(this);
        answer2.addActionListener(this);
        answer3.addActionListener(this);
        answer4.addActionListener(this);

        frame.add(panel2);
        frame.add(panel3, BorderLayout.SOUTH);
        frame.setSize(600, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        answer = e.getActionCommand(); // a global variable
        out.println(answer);
    }

    //  The main thread of the client will listen for messages
    // message in which we receive our mark. Then we go into a
    // loop listening for response from the server.
    // when the the play() end the client exit from the server
    public void play() throws Exception {
        try {

            while (true) {
                String response = in.readLine();
                if (response.endsWith("turn!")) {
                    answer1.setText("1");
                    answer2.setText("2");
                    answer3.setText("3");
                    answer4.setText("4");
                }

                if (response.contains("?")) {
                    area.append(response + "\n");
                    messageLabel.setText(response);
                    String response1 = in.readLine();
                    String response2 = in.readLine();
                    String response3 = in.readLine();
                    String response4 = in.readLine();

                    area.append(response1 + "\n" + response2 + "\n" + response3 + "\n" + response4 + "\n");

                } else {
                    area.append(response + "\n");

                }

                panel2.add(area);
                frame.add(panel2);
                frame.setLayout(null);

                if (response.endsWith("WON!") || response.endsWith("no winner!") || response.endsWith("loss")) {
                    out.println("end");
                    answer1.setVisible(false);
                    answer2.setVisible(false);
                    answer3.setVisible(false);
                    answer4.setVisible(false);
                    messageLabel.setText("Game end");
                    break;
                }
            }
        } finally {
            in.close();
            out.close();
            socket.close();
        }
    }

    /**
     * @param args the command line arguments
     */
    //run the client
    public static void main(String[] args) throws Exception {

        String serverAddress = (args.length == 0) ? "localhost" : args[1];

        knowledgeGameClient client = new knowledgeGameClient(serverAddress);
        client.play();

    }
}
