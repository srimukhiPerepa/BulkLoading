import java.io.*;
import org.apache.commons.text.StringEscapeUtils;
 
public class FileToString 
{
  public static void main(String[] args)
    throws IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader("Source.xml"));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;
    String ls = System.getProperty("line.separator");
    while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
    }
    // delete the last new line separator
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    reader.close();

    String content = stringBuilder.toString();
    String escaped = StringEscapeUtils.escapeJava(content);
    String unixText = escaped.replace("\\r\\n", "\\n"); // remove windows new lines and some escaped characters

    try (PrintWriter out = new PrintWriter("out.xml")) {
        out.println(unixText);
    }
  }
}