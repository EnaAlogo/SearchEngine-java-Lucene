package IR_ERGASIA;


import java.util.Iterator;

//////////////////////////////////////// NOT USED DEN IKSERA OTI I JAVA EXEI HASHMAP //////////////////////////////////////////////////////////
public class UnorderedMap<K , V> implements Iterable<UnorderedMap.Entry<K,V>>
{
	private int size;
	private LinkedHashList<K,V>[] buckets;
	private int bsize;
	
	
	
	@SuppressWarnings("unchecked")
	public UnorderedMap() 
	{
		bsize=8;
		buckets = new LinkedHashList[bsize];
		for(int i =0 ; i<bsize; i++)
			buckets[i]=new LinkedHashList<K,V>();
		size = 0;
		
	}
	
	
	public Object[] toPairs()
	{
		Object[] map = new Object[size()*2];
		int i=0;
		for(var entry : this)
		{
			map[i++]=entry.key();
			map[i++]=entry.value();
		}
		return map;
	}
	
	public V find(K key)
	{
		int hash = ptr(key)%bsize;
		try
		{
			return buckets[hash].get(key);
			
		}
		catch(CollisionError e)
		{
			return null;
		}
	}
	public boolean erase(K key)
	{
		int hash = ptr(key)%bsize;
		try
		{
			buckets[hash].delete(key);
			size--;
			if(size < .7*(bsize/2))
			{
				_rearrange(bsize/2);
			}
			return true;
		}
		catch(CollisionError e)
		{
			return false;
		}
	}
	public void insert(K key , V value)
	{
		
		int hash = ptr(key)%bsize;
		if(buckets[hash].swap(new Entry<K,V>(key,value))) {
			size++;
			if(size > .75*bsize)
				_rearrange(bsize*2);
		}
		
	}
	public boolean emplace(K key , V value)
	{
		int hash = ptr(key)%bsize;
		try {
			buckets[hash].insert(new Entry<K,V>(key , value));
			size++;
			if(size > .75*bsize)
				_rearrange(bsize*2);
			return true;
		} catch (CollisionError e) {
			return false;
		}
	}
	
	private void _rearrange(int newbsize)
	{
		LinkedHashList<K,V>[] newbuckets = new LinkedHashList[newbsize];
		for(int i =0 ; i <newbsize ; i++)
		{
			newbuckets[i]=new LinkedHashList<K,V>();
		}
		for(var entry : this)
		{
			int hash = ptr(entry.key());
			newbuckets[hash%newbsize].swap(entry);
		}
		buckets=null;
		buckets = newbuckets;
		bsize=newbsize;
		System.gc();
		
	}
	private int ptr(K key)
	{
		int hash = key.hashCode();
		return Math.abs(hash);
	}
	public int size()
	{
		return size;
	}
	 @Override
	    public Iterator<UnorderedMap.Entry<K,V>> iterator() 
	 {
	        Iterator<UnorderedMap.Entry<K,V>> iterator = new Iterator<UnorderedMap.Entry<K,V>>() 
	        {
	            private int index = 0;
	            private int isize = size();
	            private LinkedHashList.node<K, V> it=buckets[index].head();

	            @Override
	            public boolean hasNext() 
	            {
	                return isize > 0;
	            }
	            
	            private Entry<K,V> _next() throws CollisionError
	            {
	            	if(it == null)
	            		throw new CollisionError("next bucket");
	            	Entry<K,V> item = it.val;
	            	it= it.next;
	            	return item;
	            }

	            @Override
	            public Entry <K,V> next() 
	            {
	                try
	                {
	                	var entry = _next();
	                	isize--;
	                	return entry;
	                }
	                catch(CollisionError e)
	                {
	                	it=buckets[++index].head();
	                	return next();
	                }
	            }

	            @Override
	            public void remove() {

	            }
	        };

	        return iterator;
	    }
	 protected static record Entry<K,V>(
			 K key,
			 V value
			 ){
		 public String toString()
		 {
            return "{ "+key+" , "+value+" }";
		 }
	 }
	 
	protected static class CollisionError extends Exception
	{
		private static final long serialVersionUID = 1L;

		public CollisionError(String msg)
		{
			super(msg);
		}
	}
	private static class LinkedHashList<K,V> implements Iterable<LinkedHashList.node<K,V>>
	{
		private node<K,V> head;
		
		public LinkedHashList()
		{
			head=null;
		}
		public node<K,V> head()
		{
			return head;
		}
		public void delete(K key) throws CollisionError
		{
			if( empty() )
				throw new CollisionError("item doesnt exist");
			if(head.val.key().equals(key))
			{
				head=null;
				return;
			}
			node<K,V> it = head;
			while(it.next!=null)
			{
				if(it.next.equals(key))
				{
					node<K,V> nxt = it.next.next;
					it.next.next=null;
					it.next=nxt;
					return;
				}
				it=it.next;
			}
			throw new CollisionError("item doesnt exist");
		}
		public V get(K key) throws CollisionError
		{
			if(empty())throw new CollisionError("item doesnt exist");
			
			for(var entry : this)
			{
				
				if(entry.val.key().equals(key))
					return entry.val.value();
			}
			throw new CollisionError("item doesnt exist");
		}
		
		public boolean swap(Entry<K,V>item )
		{
			if( empty() ) {
				head= new node<K,V>(item , null);
				return true;
			}
			node<K,V> last=null;
			for(var entry : this )
			{
				last= entry;
				if(entry.val.key().equals(item.key())) {
					assert item.value() instanceof Entry;
					entry.val= item;
					return false;
				}
			}
			assert last != null;
			last.next= new node<K,V>(item , null);
			return true;
			
		}
		
		public void insert(Entry<K,V> item) throws CollisionError
		{
			if( empty() ) {
				head= new node<K,V>(item , null);
				return;
			}
			
			node<K,V> it = head;
			while(it.next!=null)
			{
				if(it.equals(item.key())) {
					throw new CollisionError("key exists");
				}
				it=it.next;
				
			}
			it.next= new node<K,V>(item , null);
			
		}
		public boolean empty()
		{
			return head==null;
		}
		protected static class node<K,V>
		{
			Entry<K,V> val;
			node<K,V> next;
			public node(Entry<K,V> val , node<K,V> next)
			{
				this.val=val;
				this.next=next;
			}
		}
		@Override
	    public Iterator<node<K,V>> iterator() {
			//assert !empty();
	        Iterator<node<K,V>> iterator = new Iterator<node<K,V>>() {
	            private node<K,V> it = head;
	            @Override
	            public boolean hasNext() {
	                return it!=null;
	            }


				@Override
	            public node<K,V> next() {
	            	var Node = it;
	                it=it.next;
	                assert Node.val instanceof Entry;
	                assert Node instanceof node<K,V>;
	                return Node;
	            }

	            @Override
	            public void remove() {

	            }
	        };

	        return iterator;
	    }
	}
	

}
