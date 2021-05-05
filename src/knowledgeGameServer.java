

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class knowledgeGameServer
{

/**
* @param args the command line arguments
* @throws java.lang.Exception
*/

static BufferedReader input1;
PrintWriter output1;

public static void main(String[] args) throws Exception
{

ServerSocket listener = new ServerSocket(8050); //TCP socket
int session = 1; //count number of session

Date date = new Date();


TextArea taLog = new TextArea(date + ":Server started at socket " + listener.getLocalPort() + "\n");


java.io.File file = new java.io.File("quiz.txt");
Scanner input = new Scanner(file);
String question = "";
//quiz array contain the quiz file
String[][] quiz = new String[10][2];
int j = 0;
int k = 1;
int n = 0;
while (input.hasNext())
{
question = input.nextLine();
if (!(k % 2 == 0))
{
quiz[j][0] = question;
}
else
{

quiz[j][1] = question;
j++;
}
k++;
}
try
{
int counter = 0;
while (true)
{
Game game = new Game(); //object of game
ImageIcon icon = new ImageIcon("icon4.jpg");
game.frame.setIconImage(icon.getImage());
//GUI elements textArea,frame,panel,
game.area.setFont(game.f);
game.panel.setBackground(new Color(164, 209, 242));
game.frame.getContentPane().setBackground(Color.LIGHT_GRAY);
game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
game.frame.setSize(800, 800);
game.frame.setVisible(true);
game.frame.setResizable(false);
game.panel.add(game.area);
game.frame.add(game.panel, BorderLayout.NORTH);
game.frame.setVisible(true);
if (counter == 0)
{
game.area.append(date + ":Server started at socket " + listener.getLocalPort() + "\n");
}
game.takefile(quiz); //method in game take quiz file
game.area.append("Wait for players to join session " + session + "\n");
//Listen for a new connection request
Socket socket1 = listener.accept();
Game.Player player1 = game.new Player(socket1, '1');
game.area.append(date + ": player " + player1.mark + " joined session " + session + "\n");
InetAddress ip = socket1.getInetAddress();
game.area.append("player " + player1.mark + "'s IP address " + ip.getHostAddress() + "\n");
player1.output.println("waiting for palyer 2 to join!");
Socket socket2 = listener.accept(); //Listen for a new connection request
InetAddress ip2 = socket2.getInetAddress();
Game.Player player2 = game.new Player(socket2, '2'); //after connecting player 2 the thread is start for specific session
game.area.append(date + ": player " + player2.mark + " joined session " + session + "\n");
game.area.append("player" + player2.mark + "'s IP address " + ip2.getHostAddress() + "\n");
game.area.append(date + ": Start a thread for session" + session + "\n");

session++;
//so we can make switches
player1.setOpponent(player2);
player2.setOpponent(player1);
player2.output.println("waiting for palyer 1 to start the game");
player2.opponent.output.println("Player 2 has joined. You start first!");
game.currentPlayer = player1; //let game start with player1

player1.start(); //run when all clients connected
player2.start();
counter++;

}
}
finally
{
listener.close();
}
}
}

class Game
{
static TextArea area = new TextArea(35, 90);
static Font f = new Font("Serif", Font.PLAIN, 15);
static JFrame frame = new JFrame("Knowledge Game Server");

static JPanel panel = new JPanel();
String quizFile[][];
Player currentPlayer;
final int count = 9; //number of question-1
int counter = 0;
//method that takes TextArea
public void txt(TextArea area)
{
this.area = area;
}
//method that takes quiz file
public void takefile(String file[][])
{
this.quizFile = file;

}
//check if the answer correct or not
public boolean checkAnswer(String choice, int index)
{ //check if the answer true
if (choice.equals(quizFile[index][1]))
{
return true;
}
else
{
return false;
}
}
//check if the client choose legal answer
public synchronized boolean legalChoice(Player player, String choice, int counter)
{

if (player == currentPlayer )
{
currentPlayer.answerdQuestion++;
if (checkAnswer(choice, counter))
{
player.output.println("you Answered question No." + (counter + 1) + "correctly!");
currentPlayer.numberOfTrue++;
}
else
{
player.output.println("WRONG Answer for question No." + (counter + 1));
}
currentPlayer.otherPlayerMoved(choice, counter);
return true;

}
return false;
}
// The class for the helper threads in this multithreaded server to handel multiple session at the same time
// application. A Player is identified by a character mark
// which is either '1' or '2'. For communication with the
// client the player has a socket with its input and output
// streams. Since only text is being communicated we use a
// Reader and a writer
class Player extends Thread
{

char mark;
Player opponent;
Socket socket;
int answerdQuestion = 0; //count to count the number of answered question
BufferedReader input;
PrintWriter output;
int numberOfTrue = 0; //count to count the number of true



//Constructs a handler thread for a given socket and mark
public Player(Socket socket, char mark)
{
this.socket = socket;
this.mark = mark;
try
{

input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
output = new PrintWriter(this.socket.getOutputStream(), true);
Date date = new Date();
output.println(date + " Connecting to server");
output.println("you are player " + this.mark);
}
catch (IOException e)
{

}
}
//method to switch turn between players
public synchronized void switchPlayers(Player player)
{
player.opponent.resume();
currentPlayer = currentPlayer.opponent; ///update current player and give the turn to the opponent
player.suspend();
}
//method to print the feedback from server to clients
public void otherPlayerMoved(String choice, int counter)
{

currentPlayer.opponent.output.println(checkAnswer(choice, counter) ? "player " + currentPlayer.mark + " answered question No." + (counter + 1) + "Correctly!" : "player " + currentPlayer.mark + "WRONG ANSWER for question No." + (counter + 1));

}
//method to set oppenent
public void setOpponent(Player opponent)
{
this.opponent = opponent;
}
//method to determine the winner or the tie and end the game
public synchronized boolean endGame(Player player, int counter)
{
if (counter == 10)
{
if (player.numberOfTrue > player.opponent.numberOfTrue)
{ //if the number of true for current player greater than his opponent
player.output.println("you WON!");
player.opponent.output.println("player" + player.mark + "has WON!");
}
if (player.numberOfTrue == player.opponent.numberOfTrue)
{ //when both player have same score
player.output.println("Game is over,no winner!");
player.opponent.output.println("Game is over,no winner!");
}
return true;
}
else if (player.opponent.numberOfTrue > (player.numberOfTrue + (5 - player.answerdQuestion)))
{
//if the first one answered 3 true question and his opponent answer 3 false question then the game end with player one as a winner
player.opponent.output.println("you WON!");
player.output.println("player " + player.opponent.mark + " has WON!");
return true;
}
else if (player.numberOfTrue > (player.opponent.numberOfTrue + (5 - player.opponent.answerdQuestion)))
{
//if the first one answered 3 false question and his opponent answer 3 true question then the game end with player one as a winner
player.output.println("you WON!");
player.opponent.output.println("player " + player.mark + " has WON!");
return true;
}

return false;

}
//The run method of this thread
@Override
public void run()
{


String answer = "";
try
{
currentPlayer.opponent.suspend();
while (true)
{
if (currentPlayer.opponent == null || currentPlayer == null )
{
counter = 0;
break;
}
if(counter!=10 &&!(currentPlayer.numberOfTrue > (currentPlayer.opponent.numberOfTrue + (5 - currentPlayer.opponent.answerdQuestion)))){
area.append("Now it is player " + this.mark + " turn!\n");

currentPlayer.output.println("Its now your turn!");
currentPlayer.opponent.output.println("Its now player " + currentPlayer.mark + " turn!");
}
else
break;
String s = quizFile[counter][0].replace(" ", "\n");
output.println(s.replace("*", " "));
answer = input.readLine(); //take the answer from the client
legalChoice(this, answer, counter);
counter = counter + 1;
endGame(this, counter);


switchPlayers(this);

}

}
catch (IOException e)
{}
finally
{
try
{
input.close();
output.close();
socket.close();
}
catch (IOException e)
{}
}
}
}
}



/*

* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

/**
*
* @author WinDows
*/


	
	
	
