import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import  java.util.Hashtable;
import  javax.naming.*;
import  javax.naming.directory.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
@WebServlet("/download") 
public class DownloadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		      throws ServletException, java.io.IOException 
		   	{
		   	
		   	}
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        String fileName = "C:\\\\Scrubber\\\\Downloadable\\\\";
        FileInputStream fileInputStream = null;
        OutputStream responseOutputStream = null;
        try
        {
            String filePath = request.getServletContext().getRealPath("/WEB-INF/resources/")+ fileName;
            File file = new File(filePath);
            
            String mimeType = request.getServletContext().getMimeType(filePath);
            if (mimeType == null) {        
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setContentLength((int) file.length());
 
            fileInputStream = new FileInputStream(file);
            responseOutputStream = response.getOutputStream();
            int bytes;
            while ((bytes = fileInputStream.read()) != -1) {
                responseOutputStream.write(bytes);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            fileInputStream.close();
            responseOutputStream.close();
        }
    }
}