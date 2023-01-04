package IR_ERGASIA_API;

import IR_ERGASIA.*;

import org.rapidoid.annotation.Valid;
import org.rapidoid.http.MediaType;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.setup.App;
import org.rapidoid.setup.On;
import java.util.Map;

public class ir_api 
{
    
		
	public static void main(String... args)
	{
	
		App.bootstrap(args).jpa();
		On.address("http://localhost").port(8080);
	
		On.get("/query").json(
				(Req request ) -> 
				{
     				init(request);
					Map<String, Object> data= request.data();
	         
					return  search_engine.search(data.get("query").toString() , data.get("order_by").toString()
							,Integer.parseInt(data.get("rescount").toString()));
				});
		
		On.get("/fopen").json( 
				(Req request) ->
				{
					init(request);
					
					Map<String, Object> data= request.data();
					
					return search_engine.fopen(data.get("key").toString(),Integer.parseInt(data.get("docID").toString()));
				});
		
		On.get("/stats").json(
				(Req request) ->
				{
					init(request);
					return search_engine.get_index_stats();
				});
		
		On.get("/stemcall").json(
				(Req request) ->
				{
					init(request);
					Map<String,Object>data=request.data();
					return search_engine.stem(data.get("word").toString());
				});
		
		On.get("/topterms").json(
		       (Req request)->
		      {
		    	  init(request);
		    	  Map<String,Object>data=request.data();
		    	  return search_engine.top_terms( Integer.parseInt(data.get("num_terms").toString()) ,
		    			  Integer.parseInt(data.get("choice").toString()) );
			
		      });
				
		On.options("/*").json(
				(Req request) -> 
				{
			       Resp response = init(request);
			       response.body("".getBytes());
			       return response;
		        });

	}
	private static Resp init(Req req)
	{
		Resp resp= req.response();
		resp.header("Access-Control-Allow-Headers", "*");
	    resp.header("Access-Control-Allow-Origin", CONSTANTS.$CLIENT_SOCKET);
	    resp.contentType(MediaType.APPLICATION_JSON);
	    resp.header("dataType","json");
	    return resp;
	}
}
