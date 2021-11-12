import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.directory.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.io.FileUtils;

import javax.servlet.annotation.*;
import com.amazonaws.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;



/**
 * Servlet implementation class UploadServlet
 */
@WebServlet("/UploadServlet")
@MultipartConfig( 
fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
maxFileSize = 1024 * 1024 * 50, // 50 MB
maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class UploadServlet extends HttpServlet {
	   
	   private static final long serialVersionUID = 1L;
	   private String filePath;
	   private String LocalFilePath="Scrubber/temp/";
	   public void init( ){
	      // Get the file location where it would be stored.
	      filePath = getServletContext().getInitParameter("file-upload"); 
	   }
	   
	   public void doPost(HttpServletRequest request, HttpServletResponse response)
	      throws ServletException, java.io.IOException 
	   	{
		   	File PDir=new File(request.getServletContext().getRealPath("")+"/Scrubber/ScrubFiles/");
		   	PDir.createNewFile();
		   	File PDomain=new File(request.getServletContext().getRealPath("")+"/Scrubber/ScrubDomain/");
		   	PDomain.createNewFile();
		   	File CDomains=new File(request.getServletContext().getRealPath("")+"/Scrubber/CachedDomains/");
		   	CDomains.createNewFile();
		   	File InvalidDomains=new File(request.getServletContext().getRealPath("")+"/Scrubber/InvalidDomains/");
		   	InvalidDomains.createNewFile();
		   	FileUtils.cleanDirectory(PDir);
		   	FileUtils.cleanDirectory(PDomain);
		   	List<String> bademailkeys=getObjectslistFromFolder("blitz-email-scrub","bad-emails");
		   	for(String bek: bademailkeys.subList(1, bademailkeys.size()))
		   	{
		   		System.out.println("* "+bek);
		   		s3downloading(bek,"blitz-email-scrub" ,request.getServletContext().getRealPath("")+"/Scrubber/ScrubFiles/");
		   		System.out.println("downloaded "+bek);
		   	}
		   	List<String> sskeys=getObjectslistFromFolder("blitz-email-scrub","salt-suppression");
		   	for(String bek: sskeys.subList(1, sskeys.size()))
		   	{
		   		System.out.println("* "+bek);
		   		s3downloading(bek,"blitz-email-scrub" ,request.getServletContext().getRealPath("")+"/Scrubber/ScrubFiles/");
		   		System.out.println("downloaded "+bek);
		   	}
		   	List<String> ssdkeys=getObjectslistFromFolder("blitz-email-scrub","suppressed-domains");
		   	for(String bek: ssdkeys.subList(1, ssdkeys.size()))
		   	{
		   		System.out.println("* "+bek);
		   		s3downloading(bek,"blitz-email-scrub" ,request.getServletContext().getRealPath("")+"/Scrubber/ScrubDomain/");
		   		System.out.println("downloaded "+bek);
		   	}
		   	File[] PurgeDomain=PDomain.listFiles();
		    File[] PurgeList=PDir.listFiles();
		    File[] cdFiles=CDomains.listFiles();
		    File[] idFiles=InvalidDomains.listFiles();
	    	String purgeFileLine=null;
	    	String purgeDomainFile=null;
	    	String cDomain=null;
	    	String iDomain=null;
	    	FileInputStream PurgeFileIS=null; //purge file input stream
	    	Scanner PFISSC=null; //purge file input stream scanner
	    	Set<String> purge=new HashSet<String>();
	    	Set<String> purgeDomain=new HashSet<String>();
	    	Set<String> CachedDomains=new HashSet<String>();
	    	Set<String> iDomains=new HashSet<String>();

	    	//purge emails adding to "purge"
	    	try
			  {
				  for(File Purge:PurgeList)
				  	{
					  PurgeFileIS=new FileInputStream(Purge.getPath());
					  PFISSC=new Scanner(PurgeFileIS,"UTF-8");
					  while(PFISSC.hasNextLine())
					  {
						purgeFileLine=PFISSC.nextLine();
						purge.add(purgeFileLine.split(",")[0].toLowerCase());  
					  }
				  	}
			  }
	    	finally
	    	{
	    		  if(PurgeFileIS!=null)
	    		  {
	    			  PurgeFileIS.close();
	    		  }
	    		  if(PFISSC!=null)
	    		  {
	    			  PFISSC.close();
	    		  } 
	    	}
	    	//purge domains adding to list "purgeDomain"
	    	try
			  {
				  for(File Purge:PurgeDomain)
				  	{
					  PurgeFileIS=new FileInputStream(Purge.getPath());
					  PFISSC=new Scanner(PurgeFileIS,"UTF-8");
					  while(PFISSC.hasNextLine())
					  {
						purgeDomainFile=PFISSC.nextLine();
						purgeDomain.add(purgeDomainFile.split(",")[0].toLowerCase());  
					  }
				  	}
			  }
	    	finally
	    	{
	    		  if(PurgeFileIS!=null)
	    		  {
	    			  PurgeFileIS.close();
	    		  }
	    		  if(PFISSC!=null)
	    		  {
	    			  PFISSC.close();
	    		  } 
	    	}
	    	try
			  {
				  for(File Cfile:cdFiles)
				  	{
					  PurgeFileIS=new FileInputStream(Cfile.getPath());
					  PFISSC=new Scanner(PurgeFileIS,"UTF-8");
					  while(PFISSC.hasNextLine())
					  {
						cDomain=PFISSC.nextLine();
						CachedDomains.add(cDomain.split(",")[0].toLowerCase());  
					  }
				  	}
			  }
	    	finally
	    	{
	    		  if(PurgeFileIS!=null)
	    		  {
	    			  PurgeFileIS.close();
	    		  }
	    		  if(PFISSC!=null)
	    		  {
	    			  PFISSC.close();
	    		  } 
	    	}
	    	try
			  {
				  for(File idFile:idFiles)
				  	{
					  PurgeFileIS=new FileInputStream(idFile.getPath());
					  PFISSC=new Scanner(PurgeFileIS,"UTF-8");
					  while(PFISSC.hasNextLine())
					  {
						iDomain=PFISSC.nextLine();
						iDomains.add(iDomain.split(",")[0].toLowerCase());  
					  }
				  	}
			  }
	    	finally
	    	{
	    		  if(PurgeFileIS!=null)
	    		  {
	    			  PurgeFileIS.close();
	    		  }
	    		  if(PFISSC!=null)
	    		  {
	    			  PFISSC.close();
	    		  } 
	    	}
	    	
		    Part filePart = request.getPart("file");
		    String fileName = extractFileName(filePart);
		    fileName=new File(fileName).getName();
		    for (Part part : request.getParts()) {
		      part.write(request.getServletContext().getRealPath("")+"/temp/"+fileName);
		    }
		    response.getWriter().println(request.getServletContext().getRealPath(""));
		    response.getWriter().print("The file uploaded sucessfully.");
		    response.getWriter().print("Currently Scrubbing."+fileName);
		    ScrubIt(request,response,fileName,purge,purgeDomain,CachedDomains,iDomains);
		    response.getWriter().println("Your File has been successfully scrubbed");
	   	}
	      
	      
	   public void doGet(HttpServletRequest request, HttpServletResponse response)
	         throws ServletException, java.io.IOException 
	   {
	         throw new ServletException("GET method used with " +
	            getClass( ).getName( )+": POST method required.");   
	   }
		private void ScrubIt(HttpServletRequest request, HttpServletResponse response,String filePath,Set<String> purge, Set<String> purgeDomain,Set<String> cDomains,Set<String> iDomains)
	    		  throws ServletException, java.io.IOException
	      {
			  File create=new File(request.getServletContext().getRealPath("")+"/Scrubber/Downloadable/");
			  create.createNewFile();
			  PrintWriter gpw=new PrintWriter(new File(request.getServletContext().getRealPath("")+"/Scrubber/Downloadable/GoodEmails.csv"));
			  gpw.println("email");
			  
			  PrintWriter ppw=new PrintWriter(new File(request.getServletContext().getRealPath("")+"/Scrubber/Downloadable/PurgedEmails.csv"));
			  ppw.println("email,reason");
			  
			  FileWriter cdm=new FileWriter(request.getServletContext().getRealPath("")+"/Scrubber/CachedDomains/ValidDomains.csv",true);
			  BufferedWriter cout= new BufferedWriter(cdm);
			  
			  FileWriter idm=new FileWriter(request.getServletContext().getRealPath("")+"/Scrubber/InvalidDomains/InvalidDomains.csv",true);
			  BufferedWriter idout= new BufferedWriter(idm);
			  int rcount=0;
			  int ccount=0;
			  File Dir=new File("Scrubber/ScrubFiles/");
			  File[] PurgeList=Dir.listFiles();
	    	  FileInputStream UserFileIS =null; //user file input stream
	    	  FileInputStream PurgeFileIS=null; //purge file input stream
	    	  Scanner PFISSC=null; //purge file input stream scanner
	    	  Scanner UFISSC=null; //user file input stream scanner
	    	  String userFileLine=null;
	    	  String userFileEmail=null;
	    	  try
	    	  {
	    		  UserFileIS=new FileInputStream(request.getServletContext().getRealPath("")+"/temp/"+filePath);
	    		  UFISSC=new Scanner(UserFileIS,"UTF-8");
	    		  while(UFISSC.hasNextLine())
	    		  {
	    			  userFileLine=UFISSC.nextLine();
	    			  userFileEmail=userFileLine.split(",")[0];
	    			  try
	    			  {
		    			  if(!simpleVal(userFileEmail))//simple validation for formatting. 
		    			  {
		    				  rcount++;
		    				  System.out.println(userFileEmail+" Invalid Email String");
		    				  ppw.println(userFileEmail+",Invalid Email String");
		    				  //add purge to purge file
		    				  //response.getWriter().print(userFileEmail+" Invalid Email String");
		    				  
		    				  continue;
		    				  
		    			  }
	    				 
		    			  if(!validDomain(userFileEmail,purgeDomain))//validate domain of user email
		    			  {
		    				  rcount++;
		    				  System.out.println(userFileEmail+" Supressed Domain");
		    				  ppw.println(userFileEmail+",Supressed Domain");
		    				  //add purge to purge file
		    				  //response.getWriter().print(userFileEmail+" Supressed Domain");
		    				  continue;
		    			  }
	    				  if(iDomains.contains(userFileEmail.toLowerCase().split("@")[1]))
	    				  {
	    					    rcount++;
	    						System.out.println(userFileEmail+" Domain Previously Invalidated");
	    						ppw.println(userFileEmail+",Domain Previously Invalidated");
			    				
	    						
	    						//response.getWriter().print(userFileEmail+",Domain Previously Invalidated");

	    						continue;
	    				  }
		    			  if(!cDomains.contains(userFileEmail.toLowerCase().split("@")[1]))
		    			  {
		    				  try 
		    			  		{
		    					  if(lookupMailHosts(userFileEmail.toLowerCase().split("@")[1]).length<=1)
		    					  {
		    						  rcount++;
		    						  System.out.println(userFileEmail+" Domain Lacks MX Records");
		    						  ppw.println(userFileEmail+",Domain Lacks MX Records");
			    					  //response.getWriter().print(userFileEmail+",Domain Previously Invalidated");
		    						  purgeDomain.add(userFileEmail.toLowerCase().split("@")[1]);
			    					  idout.write(userFileEmail.toLowerCase().split("@")[1].toString());
			    					  idout.newLine();
		    						  //add purge to purge file
			    				
		    						  continue;
		    					  }
		    					  else
		    					  {
		    						  cDomains.add(userFileEmail.toLowerCase().split("@")[1]);//add domain to list of valid domains to speed things up
		    						  cout.write(userFileEmail.toLowerCase().split("@")[1].toString());
		    						  cout.newLine();
		    					  }
		    			  		}	 
		    			  		catch (NamingException e) {
		    			  			// TODO Auto-generated catch block
		    			  			rcount++;
		    						System.out.println(userFileEmail+" Domain Invalid or Not Reachable");
		    						ppw.println(userFileEmail+",Domain Invalid or Not Reachable");
			    					
		    						//response.getWriter().print(userFileEmail+",Domain Invalid or Not Reachable");

		    						purgeDomain.add(userFileEmail.toLowerCase().split("@")[1]);
		    						idout.write(userFileEmail.toLowerCase().split("@")[1].toString());
		    						idout.newLine();
		    			  			e.printStackTrace();
		    			  			continue;
		    			  			}
		    				  
		    			  }
		    			  if(purge.contains(userFileEmail.toLowerCase()))//cleans off of global suppression lists
	    				  {
	    					  rcount++;
	    					  System.out.println(userFileEmail+" Supressed Email");
	    					  ppw.println(userFileEmail+",Supressed Email");
	    					//add purge to purge file
	    					  
	    					  //response.getWriter().print(userFileEmail+",Supressed Email");

	    					  continue;
	    					  
	    				  }
	    				  else
	    				  {
	    					  ccount++;
	    					  gpw.println(userFileEmail);
	    				  } 
	    				  //cleaning done add to clean file
	    			  }
	    			  catch(Exception e)
	    			  {
	    				  response.getWriter().println("\n\n1  "+e+"\n");
	    			  }
	    			  finally
	    			  {
	    	    		  //response.getWriter().print(userFileEmail+",Ended");

	    	    		  if(PurgeFileIS!=null)
	    	    		  {
	    	    			  PurgeFileIS.close();
	    	    		  }
	    	    		  if(PFISSC!=null)
	    	    		  {
	    	    			  PFISSC.close();
	    	    		  } 
	    			  }
	    		  }
	    	  }
	    	  catch (Exception e)
	    	  {
				  response.getWriter().println("\n\n2  "+e+"\n");

	    	  }
	    	  finally
	    	  {
	    		  //response.getWriter().print(userFileEmail+",Ended");
	    		  if(UserFileIS!=null)
	    		  {
	    			  UserFileIS.close();
	    		  }
	    		  
	    		  if(UFISSC!=null)
	    		  {
	    			  UFISSC.close();
	    		  }
	    	  }
	    	  gpw.close();
	    	  ppw.close();
    		  cout.close();
    		  idout.close();
    		  String result=upload("Scrubber/Downloadable/GoodEmails.csv","Scrubber/Downloadable/PurgedEmails.csv");
    		  	if (!result.contains("COULD NOT UPLOAD"))
    		  		{
    		  		response.getWriter().println("AWS S3 File Uploaded to - "+result);
    		  		}
    		  	else
    		  		{
    		  		response.getWriter().println("result");
    		  		}
	    	  	response.getWriter().println("\n file cleaned with " + rcount+ " removed and "+ ccount +" clean ");
	    	  	//response.getWriter().println("Your download will start immediatly");
	    	  return;
	      }
		  public static String upload(String good_file_path,String bad_file_path)
		  {
			  final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
			  try {
				  	String time=java.time.LocalDate.now()+"_"+java.time.LocalTime.now();
				    s3.putObject("blitz-email-scrub", "ScrubbedEmails/GoodEmails"+time+".csv", new File(good_file_path));
				    s3.putObject("blitz-email-scrub", "ScrubbedEmails/BadEmails"+time+".csv", new File(bad_file_path));
				    return "blitz-email-scrub/"+"ScrubbedEmails/GoodEmails"+time+".csv"+" and blitz-email-scrub/"+"ScrubbedEmails/BadEmails"+time+".csv";
			  } catch (AmazonServiceException e) {
				    System.err.println(e.getErrorMessage());
				    return "COULD NOT UPLOAD "+ e;
				}
			  
		  }
		  public static boolean simpleVal(String input)
		  {
			String reg="^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
			Pattern comp=Pattern.compile(reg,Pattern.CASE_INSENSITIVE);
			Matcher match=comp.matcher(input.toLowerCase());
			return match.find();
		  }
		  public static boolean validDomain(String input,Set<String> purgeDomain)
		  {
			  if(purgeDomain.contains(input.toLowerCase().split("@")[1]))
			  	{
				  return false;
			  	}
			  return true;
		  }
		    // returns a String array of mail exchange servers (mail hosts)
		    //     sorted from most preferred to least preferred
		    static String[] lookupMailHosts(String domainName) throws NamingException {
		        // see: RFC 974 - Mail routing and the domain system
		        // see: RFC 1034 - Domain names - concepts and facilities
		        // see: http://java.sun.com/j2se/1.5.0/docs/guide/jndi/jndi-dns.html
		        //    - DNS Service Provider for the Java Naming Directory Interface (JNDI)

		        // get the default initial Directory Context
		        InitialDirContext iDirC = new InitialDirContext();
		        // get the MX records from the default DNS directory service provider
		        //    NamingException thrown if no DNS record found for domainName
		        Attributes attributes = iDirC.getAttributes("dns:/" + domainName, new String[] {"MX"});
		        // attributeMX is an attribute ('list') of the Mail Exchange(MX) Resource Records(RR)
		        Attribute attributeMX = attributes.get("MX");

		        // if there are no MX RRs then default to domainName (see: RFC 974)
		        if (attributeMX == null) {
		            return (new String[] {domainName});
		        }

		        // split MX RRs into Preference Values(pvhn[0]) and Host Names(pvhn[1])
		        String[][] pvhn = new String[attributeMX.size()][2];
		        for (int i = 0; i < attributeMX.size(); i++) {
		            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
		        }

		        // sort the MX RRs by RR value (lower is preferred)
		        Arrays.sort(pvhn, new Comparator<String[]>() {
		            public int compare(String[] o1, String[] o2)
		            {
		                return (Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]));
		            }
		        });

		        // put sorted host names in an array, get rid of any trailing '.'
		        String[] sortedHostNames = new String[pvhn.length];
		        for (int i = 0; i < pvhn.length; i++) {
		            sortedHostNames[i] = pvhn[i][1].endsWith(".") ?
		                pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
		        }
		        return sortedHostNames;
		    }
		  public void download(HttpServletRequest request, HttpServletResponse response) throws IOException
		  {
		        String fileName = "Scrubber/Downloadable/";
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
	      private String extractFileName(Part part) {
	    	    String contentDisp = part.getHeader("content-disposition");
	    	    String[] items = contentDisp.split(";");
	    	    for (String s : items) {
	    	        if (s.trim().startsWith("filename")) {
	    	            return s.substring(s.indexOf("=") + 2, s.length()-1);
	    	        }
	    	    }
	    	    return "";
	    	}
	      public List<String> getObjectslistFromFolder(String bucketName, String folderKey) {
	    	  final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	    	  ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
	    	                                             .withBucketName(bucketName)
	    	                                             .withPrefix(folderKey + "/");
	    	   
	    	  List<String> keys = new ArrayList<>();
	    	   
	    	  ObjectListing objects = s3.listObjects(listObjectsRequest);
	    	   
	    	  for (;;) {
	    	      List<S3ObjectSummary> summaries = objects.getObjectSummaries();
	    	      if (summaries.size() < 1) {
	    	          break;
	    	      }
	    	  
	    	   summaries.forEach(s -> keys.add(s.getKey()));
	    	   objects = s3.listNextBatchOfObjects(objects);
	    	  }
	    	   
	    	  return keys;
	    	 }
	      public void s3downloading(String key_name,String bucket_name,String dlfolder)
	      {
	    	  System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
	    	  final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	    	  try {
	    	      S3Object o = s3.getObject(bucket_name, key_name);
	    	      S3ObjectInputStream s3is = o.getObjectContent();
	    	      FileOutputStream fos = new FileOutputStream(new File(dlfolder+"/"+key_name.substring(key_name.lastIndexOf('/')+1)));
	    	      byte[] read_buf = new byte[1024];
	    	      int read_len = 0;
	    	      while ((read_len = s3is.read(read_buf)) > 0) {
	    	          fos.write(read_buf, 0, read_len);
	    	      }
	    	      s3is.close();
	    	      fos.close();
	    	  } catch (AmazonServiceException e) {
	    	      System.err.println(e.getErrorMessage());
	    	      
	    	  } catch (FileNotFoundException e) {
	    	      System.err.println(e.getMessage());
	    	      
	    	  } catch (IOException e) {
	    	      System.err.println(e.getMessage());
	    	      
	    	  }
	      }
	   }