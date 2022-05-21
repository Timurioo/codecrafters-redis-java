import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class ServerHandler extends Thread {

  private final Socket clientSocket;
  private final RespParser parser = new RespParser();

  public ServerHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    System.out.println("Client accepted...");
    try (
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
    ) {

      String inputLine;
      StringBuilder commandsStr = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        if (!inputLine.equals("")) {
          System.out.println("inputLine = " + inputLine);
          commandsStr.append(inputLine).append("\r\n");
          System.out.println("commandsStr = " + commandsStr.toString().replace("\r", "\\r").replace("\n", "\\n"));
        } else {
          List<String> commands = parser.parseInput(commandsStr.toString());
          serveCommands(commands, out);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void serveCommands(List<String> commands, PrintWriter out) {
    for (int i = 0; i < commands.size(); i++) {
      switch (commands.get(i)) {
        case "echo": {
          logCommand(commands.get(i));
          String output = parser.convertOutput(commands.get(i + 1));
          out.print(output);
          System.out.println(output);
          break;
        }
        case "ping": {
          logCommand(commands.get(i));
          String pong = parser.convertOutput("PONG");
          out.print(pong);
          System.out.println(pong);
          break;
        }
        default: {
        }
      }
    }
  }

  private void logCommand(String respCommand) {
    System.out.printf("Handling %s...%n", respCommand);
  }
}
