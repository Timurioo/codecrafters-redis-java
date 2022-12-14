import java.util.ArrayList;
import java.util.List;

public class RespParser {

  public List<String> parseInput(String input) {
    System.out.println("Parsing input: " + input.replace("\r", "\\r").replace("\n", "\\n"));
    List<String> commandsStr = null;
    for (int i = 0; i < input.length(); i++) {
      char character = input.charAt(i);
      if (character != '\r' && character != '\n') {
        if (character == '*') {
          StringBuilder commandsSize = new StringBuilder();
          int j = i + 1;
          while (input.charAt(j) != '\r') {
            commandsSize.append(input.charAt(j));
            j++;
          }
          j += 1;
          int arraySize = Integer.parseInt(commandsSize.toString());
          commandsStr = new ArrayList<>(arraySize);
          i = incrementIdx(i, j);
        } else if (character == '$') {
          StringBuilder bulkStringSize = new StringBuilder();
          int j = i + 1;
          while (input.charAt(j) != '\r') {
            bulkStringSize.append(input.charAt(j));
            j++;
          }
          j += 2;
          int bulkStrSize = Integer.parseInt(bulkStringSize.toString());
          String message = input.substring(j, j + bulkStrSize);
          i = incrementIdx(i, j);
          i += bulkStrSize;
          commandsStr.add(message);
        } else if (character == ':') {
          commandsStr.add(String.valueOf(input.charAt(i + 1)));
          i++;
        }
      }
    }
    System.out.println("Input parsed: " + commandsStr);
    return commandsStr;
  }

  public int parseArraySize(String input) {
    int arraySize = -1;
    for (int i = 0; i < input.length(); i++) {
      char character = input.charAt(i);
      if (character != '\r') {
        if (character == '*') {
          StringBuilder commandsSize = new StringBuilder();
          int j = i + 1;
          while (input.charAt(j) != '\r' && input.charAt(j) != '\n') {
            commandsSize.append(input.charAt(j));
            j++;
          }
          j += 1;
          arraySize = Integer.parseInt(commandsSize.toString());
          i = incrementIdx(i, j);
        }
      }
    }
    return arraySize;
  }

  private int incrementIdx(int i, int j) {
    i += (j - i);
    return i;
  }

  public String convertOutput(String output) {
    StringBuilder str = new StringBuilder();
    try {
      int number = Integer.parseInt(output);
      return str.append(":").append(number).append("\r").toString();
    } catch (NumberFormatException ex) {
      return str.append("+").append(output).append("\r").toString();
    }
  }

  private boolean isaBulkString(String messages) {
    return messages.charAt(0) == '$';
  }

  private String parseMessage(String[] messages, int idx) {
    if (isaBulkString(messages[idx])) {
      return messages[idx + 1];
    } else {
      return messages[idx];
    }
  }

  public boolean parseCountableElement(String inputLine) {
    char markingChar = inputLine.charAt(0);
    return markingChar != '$';
  }

  public String convertBulkString(String s) {
    if (s == null) return "$-1\r";
    return "$" + s.length() + "\r\n" + s + "\r";
  }
}