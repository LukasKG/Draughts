import java.io.IOException;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 * The class Log is used to write the logfile with all user actions.
 *
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class Log 
{
    private static final String defaultLogFile = "log\\logfile.txt";
    
    public static void write(String s)
    {
        try
        {
            System.out.println(s);
            write(defaultLogFile, s);
        }
        catch(IOException e)
        {
            System.out.println("Can't write logfile: "+e);
        }
    }
    
    public static void write(String f, String s) throws IOException
    {
        //TimeZone timeZone = TimeZone.getTimeZone("UTC");
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat simpleDateFormat = 
               new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
        simpleDateFormat.setTimeZone(timeZone);
        String currentTime = simpleDateFormat.format(calendar.getTime());
                
        FileWriter aWriter = new FileWriter(f, true);
        aWriter.write(currentTime + " " + s + "\n");
        aWriter.flush();
        aWriter.close();
    }
}
