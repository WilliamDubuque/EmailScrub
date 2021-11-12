import java.io.IOException;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import counter.counter;
/**
 * Servlet implementation class Scrubber
 */
@WebServlet("/Scrubber")
public class Scrubber extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * Default constructor. 
     */
    int count;
    private counter counter;
    
    public Scrubber() {
    	super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession(true);
    	  session.setMaxInactiveInterval(5);
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          if (session.isNew()) {
              count++;
          }
          printPage(out,count);
          out.println();
          
	}
	@Override
    public void init() throws ServletException {
        counter = new counter();
        try {
            count = counter.getCount();
        } catch (Exception e) {
            getServletContext().log("An exception occurred in FileCounter", e);
            throw new ServletException("An exception occurred in FileCounter"
                    + e.getMessage());
        }
    }
	public void destroy() {
        super.destroy();
        try {
            counter.save(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	
	 private void printPage (PrintWriter out,int count) 
	    {
		 out.println("<html>");
	    	out.println("<head>");
	    	out.println("<title>Email Scrub</title>");
	    	printstyle(out);
	    	out.println("</head>");
	    	out.println("<body>");
	    	printBody(out, count);
	    	
	    }	    
	 private void printstyle(PrintWriter out)
	 	{
		 out.println("<style>");
		 out.println("html,\r\n" + 
		 		"body {\r\n" + 
		 		"   margin:0;\r\n" + 
		 		"   padding:0;\r\n" + 
		 		"   height:100%;\r\n" + 
		 		"}\r\n" + 
		 		"div {\r\n" + 
		 		"    outline: 1px solid;\r\n" + 
		 		"}\r\n" + 
		 		"#con {\r\n" + 
		 		"   min-height:100%;\r\n" + 
		 		"   position:relative;\r\n" + 
		 		"}\r\n" + 
		 		"#content {\r\n" + 
		 		"   height: 1000px; /* Changed this height */\r\n" + 
		 		"   padding-bottom:60px;\r\n" + 
		 		"}\r\n" + 
		 		"#footer {\r\n" + 
		 		"   position:absolute;\r\n" + 
		 		"   bottom:0;\r\n" + 
		 		"   width:100%;\r\n" + 
		 		"   height:60px;\r\n" + 
		 		"}");
		 out.println("</style>");
	 	}
	 private void printBody(PrintWriter out,int count)
	 {
		 out.println("<center>");
	    	out.println("<br>");
	    	out.println("<h1><u>Email Scrub</u></h1><br>");
	    	out.println(" <body>\r\n" + 
	    			"      <h3>File Upload:</h3>\r\n" + 
	    			"      Select a file to upload: <br />\r\n" + 
	    			"      <form action = \"UploadServlet\" method = \"post\" enctype = \"multipart/form-data\">\r\n" + 
	    			"         <input type = \"file\" name = \"file\" size = \"50\" />\r\n" + 
	    			"         <br />\r\n" + 
	    			"         <input type = \"submit\" value = \"Upload File\" />\r\n" + 
	    			"      </form>\r\n" + 
	    			"   </body>");
			out.println("<div id=\"footer\">This site has been accessed " + count + " times.</div>\r\n");
			out.println("</html>");
	 }
}
