

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.rapidoid.u.U;
import org.tartarus.snowball.ext.PorterStemmer;



public class search_engine
 {
	
	 
	 private static HashMap<String , Query >tokens=new HashMap<String , Query>();
	 
	 public static Map getCache()
	 {
		 return U.map(tokens);
	 }
	 
     private search_engine() {};
     
	 public static void index() 
	 throws IOException, URISyntaxException, java.text.ParseException
	 {
		Directory indexdir = FSDirectory.open(Paths.get(CONSTANTS.ipath));
		File[] dir = new File(CONSTANTS.fpath).listFiles(File::isDirectory);
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(CONSTANTS.analyzer);
		indexWriterConfig.setSimilarity(new ClassicSimilarity());
		IndexWriter iw = new IndexWriter(indexdir, indexWriterConfig);
		
		for (File directory : dir) {
			File[] folder = new File(directory.getPath()).listFiles();
			for (File file : folder)
				addfile( iw , file, indexdir);
		}
		
		iw.close();
	}

	 private static<T> void print(T...items)
	 {
		 for(var el : items)
			 System.out.println(el);
	 }
	 
	@SuppressWarnings("rawtypes")
	public static List<Map> search(String query , String choice , int num_results ) 
	 throws ParseException, IOException, InvalidTokenOffsetsException, java.text.ParseException
	 {
		 
		 query = query.trim().replace(":", "\\:");
		 
		 String inquery= query.replace(" ", "");
		 
		 tokens.put(inquery, makeQuery(query));
		 
		 return search( inquery , num_results ,sort(Integer.parseInt(choice)));
	
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map fopen(String key,int docid) 
			 throws IOException, InvalidTokenOffsetsException
	 {        
		 key = key.trim().replace(":", "\\:");
		 
		 String inquery = key.replace(" ", "");
		 
		 
		 IndexReader ird= DirectoryReader.open(FSDirectory.open(Paths.get(CONSTANTS.ipath)));
		 IndexSearcher src = new IndexSearcher(ird);
		 Path path= Paths.get(src.doc(docid).get(CONSTANTS.f_pth));
		 
		 HashSet terms = new HashSet<String>(); extract_terms(tokens.get(inquery) , src , docid , terms);
		 
		 BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
		 
		 UTILS.filereader it = new UTILS.filereader(br);
		 StringBuilder l = new StringBuilder();
		 
		 for(String line : it)
			 l.append(line+"<br>");
			
		 
		 
         br.close();
         String file_content= highlight(tokens.get(inquery),l.toString(),ird,src,Integer.MAX_VALUE);
    
		 return U.map("terms",terms,"file",file_content);
	 }
	 public static String stem(String word)
	 {
	    	PorterStemmer stem = new PorterStemmer();
	    	stem.setCurrent(word);
	    	stem.stem();
	    	return stem.getCurrent();
	    	
	 }
	 @SuppressWarnings("rawtypes")
     public static Map get_index_stats() 
		 throws Exception 
			{
			 Directory index = FSDirectory.open(Paths.get(CONSTANTS.ipath));
	         IndexReader rd = DirectoryReader.open(index);

	         IndexSearcher src = new IndexSearcher(rd); 
	         CollectionStatistics c_statistics =src.collectionStatistics(CONSTANTS.f_cont);
	         long total_docs = c_statistics.docCount();
	         long term_freqsum= c_statistics.sumTotalTermFreq();
	         long doc_freqsum = c_statistics.sumDocFreq();
	         
	         TermsEnum iter= MultiTerms.getTerms(rd, CONSTANTS.f_cont).iterator();
	         long termcount=0;
	         BytesRef it = iter.next();
	         
	         while(it != null){termcount++;it=iter.next();}
			 
	         return  U.map("docnum",total_docs ,"termnum",termcount,"tokens", term_freqsum ,"postings",doc_freqsum );
			}
	 
	 
	 public static List top_terms(int num_terms,int choice) throws Exception
	 {
		 Directory index = FSDirectory.open(Paths.get(CONSTANTS.ipath));
         IndexReader rd = DirectoryReader.open(index);
		 Comparator<TermStats> comperator;
		 CONSTANTS.ORDER_BY type= CONSTANTS.typecast(choice);
		 if(type == CONSTANTS.ORDER_BY.TERM)
			 comperator = new HighFreqTerms.TotalTermFreqComparator();
		 else if(type==CONSTANTS.ORDER_BY.DOC)
			 comperator = new HighFreqTerms.DocFreqComparator();
		 else throw new Exception("kek"); 
		 int top_terms=num_terms;
		 Map[] topterms = new Map[num_terms];
         TermStats[] stats = HighFreqTerms.getHighFreqTerms(rd, num_terms ,CONSTANTS.f_cont, comperator );
         
         for (TermStats termStats : stats) 
             topterms[num_terms-(top_terms--)] = U.map("term",termStats.termtext.utf8ToString() ,"tf", termStats.totalTermFreq ,"df",termStats.docFreq);
         
         return U.list(topterms);
	 }
	 
	 
	 
	 
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Map> search(String key, int max_depth,Sort sortby) 
	 throws ParseException, IOException, InvalidTokenOffsetsException 
	 {

		Directory index = FSDirectory.open(Paths.get(CONSTANTS.ipath));

		IndexReader ird = DirectoryReader.open(index);

		IndexSearcher search = new IndexSearcher(ird);
		search.setSimilarity(new ClassicSimilarity());
		
		TopDocs topscores = search.search(tokens.get(key), max_depth, sortby, true);

		var hits = topscores.scoreDocs;

		System.out.println("Found " + hits.length + " hits.");
	
		int max_char_window = 150;
		List labels = new ArrayList<Map>();
		for(ScoreDoc hit : hits)
		  labels.add(highlight(tokens.get(key),ird,search , hit ,max_char_window));
		  
		
		return U.list(labels);

	}

     private static Sort sort(int type)
     {
    	// SortField field = new SortField(SortField.FIELD_DOC,SortField.FIELD_SCORE);
    	 CONSTANTS.ORDER_BY choice = CONSTANTS.typecast(type);
    	 switch(choice)
    	 {
    	    default: throw new InvalidParameterException();
    	    
    	    case TERM :
    	    	return new Sort(SortField.FIELD_SCORE,SortField.FIELD_DOC);
    	    	
    	    case DOC : 
    	    	return new Sort(SortField.FIELD_DOC,SortField.FIELD_SCORE);
    	    case DATE:
    	    	return new Sort(new SortField(CONSTANTS.f_dl,SortField.Type.LONG),SortField.FIELD_SCORE);
    	 }
     }
	 @SuppressWarnings("rawtypes")
	private static Map highlight(Query query,IndexReader rd , IndexSearcher src, ScoreDoc hit,int max_char_window) 
	 throws IOException, InvalidTokenOffsetsException 
	 {	
		 
        var sc= new QueryScorer(query);
		SimpleHTMLFormatter htm=new SimpleHTMLFormatter();
		Highlighter hg= new Highlighter(htm ,sc);
		hg.setTextFragmenter(new SimpleFragmenter( max_char_window ));
		
		Path path=Paths.get(src.doc(hit.doc).get(CONSTANTS.f_pth));
		String fullname= src.doc(hit.doc).get(CONSTANTS.f_nam);
		String text="";
		try {
		   text =  Files.readString(path).trim().replaceAll("[\\s||\n||<>]{1,}", " ");
		}
		catch(java.nio.charset.MalformedInputException e)
		{
		   text =  Files.readString(path,StandardCharsets.ISO_8859_1).trim().replaceAll("[\\s||\n||<>]{2,}", " ");
		}
		
		String frg= hg.getBestFragment(CONSTANTS.analyzer,CONSTANTS.f_nam , text);
		
		return  U.map("fname",fullname,"abstract", frg,"relativity",hit.score,"docID", hit.doc,
				"date",UTILS.todate(Long.parseLong(src.doc(hit.doc).get(CONSTANTS.f_date))));
	 }
	 
		private static String highlight(Query query ,String text,IndexReader rd , IndexSearcher src, int max_char_window ) 
		 throws IOException, InvalidTokenOffsetsException 
		 {	
	        var sc= new QueryScorer(query);
			SimpleHTMLFormatter htm=new SimpleHTMLFormatter();
			Highlighter hg= new Highlighter(htm ,sc);
			hg.setTextFragmenter(new SimpleFragmenter( max_char_window ));
			
		    
			return  hg.getBestFragment(CONSTANTS.analyzer,CONSTANTS.f_nam , text);
		 }

	
	 private static void addfile(IndexWriter iw, File file, Directory indexdir) 
			 throws URISyntaxException, IOException, java.text.ParseException
			 {
				String name= file.getParentFile().getName()+"." +file.getName();
				long date=UTILS.get_date(file);
				Document doc = new Document();
				FileReader fread = new FileReader(file);
				doc.add(new TextField(CONSTANTS.f_cont, fread));
				doc.add(new StringField(CONSTANTS.f_pth, file.getPath(), Field.Store.YES));
				doc.add(new StringField(CONSTANTS.f_nam, name, Field.Store.YES));
				doc.add(new LongPoint(CONSTANTS.f_d,date));
				doc.add(new StoredField(CONSTANTS.f_date,date));
				doc.add(new NumericDocValuesField(CONSTANTS.f_dl,date));
				iw.addDocument(doc);
			}
	 

	@SuppressWarnings("deprecation")
	private static void extract_terms(Query query, IndexSearcher indexSearcher,
			    int docId, HashSet<String> out_hitWords) throws IOException {
		
			  if (query instanceof TermQuery)
			    if (indexSearcher.explain(query, docId).isMatch())
			    	out_hitWords.add(((TermQuery) query).getTerm().toString().split(":")[1]);
			  
			  if (query instanceof BooleanQuery) 
			    for (BooleanClause clause : (BooleanQuery) query) 
			    	extract_terms(clause.getQuery(), indexSearcher, docId, out_hitWords);

			  if (query instanceof MultiTermQuery) {
			    ((MultiTermQuery) query).setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_REWRITE);
			    extract_terms(query.rewrite(indexSearcher.getIndexReader()),indexSearcher, docId, out_hitWords);
			  }

			}
	
	  @SuppressWarnings({ "rawtypes", "unchecked" })
		private static tuple QueryPreProccess(String query) throws java.text.ParseException
	    {
	    	
	    	String dformat="[0-9]{1,2}\\s[a-zA-Z]{1,9}[\\s]{0,}\\-[\\s]{0,}[0-9]{1,2}\\s[a-zA-Z]{1,9}";
	    	
	    	String str=query.replaceAll(dformat, "");
	    	int datecheck= str.length();
	    	int strlen = query.length();
	    	
	    	if(datecheck==strlen) return null;
	    	
	    	int findex = strlen-datecheck;
	    	
	    	for(int i =0 ; i <query.length();i++)
	        	if(query.substring(i,i+findex).matches(dformat))//System.out.println(query.substring(i,i+findex));
	        		return  tuple.cat(UTILS.transform_dates(query.substring(i,i+findex)) , new tuple(str));
	    	return null;
	    	
	    }
	   
	    private static Query makeQuery(String query) throws java.text.ParseException, ParseException
	    {
	    	@SuppressWarnings("rawtypes")
			tuple Proccessed = QueryPreProccess(query);
	    	
	    	if(Proccessed==null)return new QueryParser(CONSTANTS.f_cont, CONSTANTS.analyzer).parse(query);
	    	
	    	long from = tuple._get(Proccessed,0);
	        long to = tuple._get(Proccessed, 1);
			String termQuery = tuple._get(Proccessed, 2);
		    
			Query first = LongPoint.newRangeQuery(CONSTANTS.f_d,from,to);
			Query second = new QueryParser(CONSTANTS.f_cont,CONSTANTS.analyzer).parse(termQuery);
	
			var q = new BooleanQuery.Builder();
			q.add(first,BooleanClause.Occur.MUST);
			q.add(second,BooleanClause.Occur.MUST);
			return q.build();
	    }
	    
	}



