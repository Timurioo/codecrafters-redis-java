import java.io.IOException;
import java.net.ServerSocket;

public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
    int port = 6379;
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        new ServerHandler(serverSocket.accept()).start();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
