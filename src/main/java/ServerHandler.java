import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerHandler extends Thread {

  private final Socket clientSocket;
  private final RespParser parser = new RespParser();
  private PrintWriter out;
  private BufferedReader in;

  public ServerHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    System.out.println("Client accepted...");
    try {
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      String inputLine;
      StringBuilder commandsStr = new StringBuilder();
      while (true) {
        while ((inputLine = in.readLine()) != null) {
          System.out.println("inputLine: " + inputLine);
          commandsStr.append(inputLine).append("\r\n");
          System.out.println("commandsStr: " + commandsStr.toString().replace("\r", "\\r").replace("\n", "\\n"));
        }
        List<String> commands = parser.parseInput(commandsStr.toString());
        for (int i = 0; i < commands.size(); i++) {
          switch (commands.get(i)) {
            case "echo": {
              logCommand(commands.get(i));
              String output = parser.convertOutput(commands.get(i + 1));
              out.println(output);
              System.out.println(output);
              break;
            }
            case "ping": {
              logCommand(commands.get(i));
              String pong = parser.convertOutput("PONG");
              out.println(pong);
              System.out.println(pong);
              break;
            }
            default: {}
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        in.close();
        out.close();
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void logCommand(String respCommand) {
    System.out.printf("Handling %s...%n", respCommand);
  }
}
