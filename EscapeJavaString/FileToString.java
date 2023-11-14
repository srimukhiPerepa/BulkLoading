import java.io.*;
import org.apache.commons.text.StringEscapeUtils;
 
/**
 * Usage:
 * 1. Put your Workflow XML source in Source.xml file
 * 2. Compile from this directory
 *    javac -cp ".;./commons-lang3-3.13.0.jar;./commons-text-1.11.0.jar" FileToString.java
 * 3. Execute from this directory
 *    java -cp ".;./commons-lang3-3.13.0.jar;./commons-text-1.11.0.jar" FileToString
 * 4. out.xml will now have a single line escaped string that can be used with Update Workflow API
 */
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