

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.analysis.en.EnglishAnalyzer;


public final class CONSTANTS
{
	private CONSTANTS() {};
		
	protected final static String fpath="<Path to Dataset>";//path to documents
	
	protected final static String ipath="<Path to Index>";//path to index
	
	public final static String $CLIENT_SOCKET="http://localhost:5500";
	
	
	protected final static String f_cont = "contents";
	protected final static String f_pth = "path";
	protected final static String f_nam= "filename";
	protected final static String f_dl="dateLong";
	protected final static String f_d="date";
	protected final static String f_date="storedDate";
	protected final static EnglishAnalyzer analyzer = new EnglishAnalyzer();
    
	
	protected static enum ORDER_BY{
		TERM,
		DOC,
		DATE,
	}
	
	protected static ORDER_BY typecast(int choice) 
	throws ArrayIndexOutOfBoundsException
	{
		return ORDER_BY.values()[choice];
	}
	
}
	
	
	
class UTILS{
	private UTILS() {};
	protected static long get_date(File f) 
	throws IOException, ParseException
	{
		   //bruteforce to extract 20 newsgroup dataset's dates from text
		   final String formats[]={"EEE, d MMM yyyy HH:mm:ss z","d MMM yy HH:mm:ss z","d MMM yyyy HH:mm:ss z","d MMM yyyy HH:mm z",
				"d MMM yyyy HH:mm:ss","d-MM MMMMM yyyy","MMMMM d-MM, yyyy","EEEEEE, d MMM yy HH:mm:ss z","EEE, d MMM yyyy HH:mm:ss"
				 ,"EEE, d MMM yyyy HH:mm z","d MMM yyyy HH:mm z"};
		   
		    BufferedReader read = new BufferedReader(new FileReader(f));
			filereader it = new filereader(read);
			String correctLine="";
			for(String line : it) {
			
				if(line.length()>4&&line.substring(0, 4).equals("Date"))
				{
					correctLine=line;
					break;
				}
			}
			//Fri, 23 Apr 1993 21:52:06 GMT
			read.close();
			String date = correctLine.substring(6,correctLine.length());
			if(date.contains("(2 Credits)"))
					
				date= date.substring(0,date.indexOf("(2 Credits)"));
			if(date.contains("MEZ")) {
				date=date.replace("MEZ", "GMT");
				date=date.replace("22", "23");
			}
			if(date.contains("TUR"))
			{
				date=date.replace("TUR", "GMT");
				date=date.replace("17", "14");
			}
			if(date.contains("UT"))
				date= date.replace("UT", "UTC");
			date=date.trim().replaceAll("[//s]{2,}", " ");
			Date d=null;
			
			boolean flag=false;
			int ind=0;
			do {
			try {
				 
			     d = new SimpleDateFormat(formats[ind]).parse(date);
			     flag=true;
			     ind=0;
			}
			catch(ParseException e)
			{
				
				ind++;
				
			}
			catch(ArrayIndexOutOfBoundsException er)
			{
				System.out.println(date);
				System.out.println(f.toPath());
				System.exit(0);
			}
			}while(!flag);
			
	        return d.getTime();
	};
	protected static tuple transform_dates(String range) 
	throws ParseException
	{
		 String[]dates = range.split("-");
		 java.util.Date dd = new SimpleDateFormat("d MMM").parse(dates[0]);
		 java.util.Date d2 = new SimpleDateFormat("d MMM").parse(dates[1]);
		 dd.setYear(93);
		 d2.setYear(93);
		 return new tuple(dd.getTime(),d2.getTime());
	}
	protected static Date todate(long num)
	{
		Date date= new Date(num);
		return date;
	}
	
	public static class filereader implements Iterable<String>
	{
		private BufferedReader br;
		public filereader(BufferedReader in)
		{
			br=in;
		}
		
		
		
		 @Override
		    public Iterator<String> iterator()  {
			 try {
		        Iterator<String> iterator = new Iterator<String>() {
		        	
		            private String line=br.readLine();
		        

		            @Override
		            public boolean hasNext() {
		                return line!=null;
		            }

		            @Override
		            public String next() {
		            
		                	String ret=line;
		                	try {
								line = br.readLine();
							} catch (IOException e) {
								
								line = null;
								e.printStackTrace();
							}
							return ret;
		
		            }

		            @Override
		            public void remove() {

		            }
		        };
			 

		        return iterator;
			 }catch(Exception e)
			 {
				 return null;
			 }
		    }
	}
	
	
}
	







