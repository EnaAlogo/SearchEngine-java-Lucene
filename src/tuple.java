

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class tuple<T> implements Iterable<T>
{
    private T[]items;
    
   
	@SafeVarargs
	public tuple(T...args)
    {
        items=args;
        
    }
    @SuppressWarnings({ "rawtypes", "unchecked"})
	public Map map(T... keyNames) throws Exception
    {
      if(keyNames.length!= size()) throw new Exception("Expected "+size()+" args, got "+keyNames.length);  
      Map res =  new HashMap();
      for(int i =0 ; i <  size();i++)
         res.put(keyNames[i], get(i) );
      return res;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" }) 
    public static<E> E _get(tuple tup ,int index)
    {
    	assert tup != null;
    	assert index<tup.size();
    	assert (E)tup.items[index] instanceof E;
    	return (E) tup.items[index];
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map toJSON() throws Exception
    {
    	if( (( size()>>32 )&1) == 1 ) throw new Exception("Could not be mapped in pairs");  
        Map res =  new HashMap();
        for(int i =1 ; i < size() ; i++)
           res.put(get(i-1).toString() ,get(i));
        return res;
    	
    }
    
    @SuppressWarnings("unchecked")
	public static tuple<Object> forList(tuple<Object[]> tup, int index)
    {
    	tuple<Object> res = new tuple<Object>();
    	for(Object[] el : tup)
    	{
    		assert index < el.length;
    		res = tuple.cat(res , new tuple<Object>(el[index]) );
    	}
    	return res;
    }
    public String toString()
    {
    	StringBuilder str= new StringBuilder();
    	str.append("( ");
    	for(var el : this)
    	{
    		str.append(el.toString());
    		str.append(' ');
    	}
    	str.append(')');
    	return str.toString();
    }
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static<T> tuple fill(int size , Iterable elements)
    {
		assert size>0;
    	Object[] items = new Object[size];
    	int i=0;
    	for(Object el : elements)
    		items[ i++ ] = el;
    	return new tuple(items);
    }
    public T get(int at)
    {
        return (T)items[at];
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static<T> tuple cat(tuple<T>...args)
    {
        ArrayList<T> items = new ArrayList<T>();
        for(var tup : args)
        {
            for(var item : tup)
              items.add(item);
        }
        return new tuple( items.toArray() );
    }
    
    public int size()
    {
        return items.length;
    }
    
    @Override
    public Iterator<T> iterator() {
        Iterator<T> iterator = new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < items.length;
            }

            @Override
            public T next() {
                return (T)items[index++];
            }

            @Override
            public void remove() {

            }
        };

        return iterator;
    }
    
}
