import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
    int port = 6379;
    CacheWithExpiration<String, String> cache = new CacheWithExpiration<>();
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        new ServerHandler(serverSocket.accept(), cache).start();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
