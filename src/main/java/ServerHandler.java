import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerHandler extends Thread {

  private final Socket clientSocket;
  private final CacheWithExpiration<String, String> cache;
  private final RespParser parser = new RespParser();

  public ServerHandler(Socket clientSocket, CacheWithExpiration<String, String> cache) {
    this.clientSocket = clientSocket;
    this.cache = cache;
  }

  @Override
  public void run() {
    System.out.println("Client accepted...");
    try (
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
    ) {

      StringBuilder inputLine = new StringBuilder();
      StringBuilder commandsStr = new StringBuilder();
      int arraySize = -1;
      int elementCount = 0;
      int input;
      while (true) {
        input = in.read();
        if (input == -1) {
          continue;
        }
        char character = (char) input;
        if (character != '\r' && character != '\n') {
          inputLine.append(character);
        } else if (character == '\n') {
          inputLine.append("\r\n");
          if (arraySize == -1) {
            arraySize = parser.parseArraySize(inputLine.toString());
          } else {
            boolean shouldBeCounted = parser.parseCountableElement(inputLine.toString());
            if (shouldBeCounted) {
              elementCount++;
            }
          }
          commandsStr.append(inputLine);
          inputLine = new StringBuilder();
          if (commandsStr.toString().isEmpty()) {
            continue;
          }
          if (elementCount == arraySize) {
            arraySize = -1;
            elementCount = 0;
            List<String> commands = parser.parseInput(commandsStr.toString());
            serveCommands(commands, out);
            commandsStr = new StringBuilder();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        System.out.println("Closing client connection...");
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
        case "get": {
          logCommand(commands.get(i));
          String key = commands.get(i+1);
          String value = parser.convertBulkString(cache.get(key));
          out.println(value);
          System.out.println(value);
          break;
        }
        case "set": {
          logCommand(commands.get(i));
          String key = commands.get(i+1);
          String value = commands.get(i+2);
          String expirationFlag = null;
          if (i + 3 > commands.size()) {
            expirationFlag = commands.get(i + 3);
          }
          String millisec;
          if ("px".equals(expirationFlag)) {
            millisec = commands.get(i + 4);
            System.out.println("Setting " + key + "->" + value + " expiring after " + millisec + " ms");
            cache.put(key, value, Long.parseLong(millisec));
          } else {
            System.out.println("Setting " + key + "->" + value);
            cache.put(key, value);
          }
          out.println(parser.convertOutput("OK"));
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
