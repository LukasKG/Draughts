import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.scene.image.Image;
/**
 * The class Error handles all output of error messages. 
 * Some members are static final since they are defined before and never changed
 * during runtime.
 *
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class Error
{
    /**
     * Process an occuring error. 
     * Create output and terminate if the error is fatal.
     *
     * @param terminate Whether the programm has to be terminated
     * @param msg The error message
     */
    public static final void send(boolean terminate, String msg)
    {
        printMsg(msg);
        if(terminate){System.exit(1);}
    }

    /**
     * Prints the error message.
     *
     * @param msg The error message
     */
    private static final void printMsg(String msg)
    {
        //System.out.println(" --- ERROR ---\n " + msg + ".\n -------------");
        Log.write("ERROR: "+ msg + "."); 
    }
    
    /**
     * Access the error symbol for images which cannot be found.
     *
     * @return The error symbol image
     */
    public static Image getImg()
    {
        String pathError = "img/error.png";
        Image errorImage = null;
        try
        {
            errorImage = new Image(new FileInputStream(pathError));
        }
        catch (FileNotFoundException e)
        {
            Error.send(false,"Can't find path \""+pathError+"\"\n Error: "+e);
        }
        return errorImage;
    }
}
