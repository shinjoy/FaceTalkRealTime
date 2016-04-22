package com.nomadsoft.redis;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

	static RedisClient instance = null;
	
    private JedisPool pool;

    public RedisClient() {
        try {
        	pool = new JedisPool(new JedisPoolConfig()
            , "localhost"
            , 6379);//,30000,"12345");
        } catch (Exception e) {

        }
    }        
    
    public static RedisClient getInstance() {
    	if(instance == null) {
    		instance = new RedisClient();
    	}    	
    	
    	return instance;
    }

    public interface Callback {
        public void execute(Jedis jedis);
    }

    public void operate(Callback callback) {
        Jedis jedis = borrow();
        try {
            callback.execute(jedis);
        } finally {
            revert(jedis);
        }
    }

    public String get(String key) {
        Jedis jedis = borrow();
        try {
            return jedis.get(key);
        } finally {
            revert(jedis);
        }
    }
/*
    public String bGet(String key) {
        Jedis jedis = borrow();
        try {
            byte[] value = jedis.get(key.getBytes());
            if (value != null) return GZip.decodeWithGZip(value);
            return null;
        } finally {
            revert(jedis);
        }
    }
*/
    public String set(String key, String value) {
        Jedis jedis = borrow();
        try {
            return jedis.set(key, value);
        } finally {
            revert(jedis);
        }
    }

    public void del(String key) {
        Jedis jedis = borrow();
        try {
            jedis.del(key);
        } finally {
            revert(jedis);
        }
    }
/*
    public String bSet(String key, String value) {
        Jedis jedis = borrow();
        try {
            return jedis.set(key.getBytes(), GZip.encodeWithGZip(value));
        } finally {
            revert(jedis);
        }
    }
*/
    public byte[] bGetJ(String key) {
        Jedis jedis = borrow();
        try {
            byte[] value = jedis.get(key.getBytes());
            if (value != null) return value;
            return null;
        } finally {
            revert(jedis);
        }
    }

    public String bSetJ(String key, byte[] value) {
        Jedis jedis = borrow();
        try {
            return jedis.set(key.getBytes(), value);
        } finally {
            revert(jedis);
        }
    }

    public boolean exits(String key) {
        Jedis jedis = borrow();
        try {
            return jedis.exists(key);
        } finally {
            revert(jedis);
        }
    }

    public List<String> mget(String[] keys) {
        Jedis jedis = borrow();
        try {
        	
        	
            return jedis.mget(keys);
        } finally {
            revert(jedis);
        }
    }

    public String info() {
        Jedis jedis = borrow();
        try {
            return jedis.info();
        } finally {
            revert(jedis);
        }
    }
/*
    public List<String> bMget(String[] keys) {
        Jedis jedis = borrow();
        int len = keys.length;
        byte[][] bkeys = new byte[len][];
        for (int i = 0; i < keys.length; i++) {
            bkeys[i] = keys[i].getBytes();
        }
        try {
            List<byte[]> list = jedis.mget(bkeys);
            List<String> temp_list = new ArrayList<String>(list.size());
            for (byte[] temp : list) {
                temp_list.add(GZip.decodeWithGZip(temp));
            }
            return temp_list;
        } finally {
            revert(jedis);
        }
    }
*/
    public Set<String> sCopy(String key, String new_key) {
        Jedis jedis = borrow();
        try {
            Set<String> oldSets = jedis.smembers(key);
            for (String str : oldSets) {
                jedis.sadd(new_key, str);
            }
            return oldSets;
        } finally {
            revert(jedis);
        }
    }

    public void sClear(String key, String oldKey) {
        Jedis jedis = borrow();
        try {
            Set<String> oldSets = jedis.smembers(key);
            for (String str : oldSets) {
                jedis.del(oldKey + ":" + str);
            }
            jedis.del(key);
        } finally {
            revert(jedis);
        }
    }

    public Set<String> sMove(String key, String new_key) {
        Jedis jedis = borrow();
        try {
            Set<String> oldSets = jedis.smembers(key);
            for (String str : oldSets) {
                jedis.smove(key, new_key, str);
            }
            return oldSets;
        } finally {
            revert(jedis);
        }
    }


    public void destory() {
        pool.destroy();
    }

    public Jedis borrow() {
        return pool.getResource();
    }

    public void revert(Jedis jedis) {
    	jedis.close();
        //pool.returnResource(jedis);
    }
    

	static int year = 0;
	static int month = 0;
	static long days = 0;
	
	static int befYear = 0;
	static int befMonth = 0;	
	
	public static void setDate() {
		
		Calendar cl = Calendar.getInstance();
		year = cl.get(Calendar.YEAR);			
		month = cl.get(Calendar.MONTH) + 1;		
		cl.add(Calendar.MONTH, -1);	
		
		befYear = cl.get(Calendar.YEAR);
		befMonth = cl.get(Calendar.MONTH) + 1;	
	}
	public static String getMsgKeyPrefix() {
		
		
		if(days == 0) {
			days = System.currentTimeMillis() / 86400000; //하루.
			setDate();
		} else {
			long current = System.currentTimeMillis() / 86400000;
			if(days != current) {
				days = current; 
				setDate();
			}
		}
		
		

		if(month < 10) {
			return String.valueOf(year) + "0" + String.valueOf(month);
		} else {
			return String.valueOf(year) + String.valueOf(month);
		}
	}
	
	
	public static  String getBeforeMsgKeyPrefix() {

		if(days == 0) {
			days = System.currentTimeMillis() / 86400000; //하루.
			setDate();
		} else {
			long current = System.currentTimeMillis() / 86400000;
			if(days != current) {
				days = current; 
				setDate();
			}
		}
		
		if(month < 10) {
			return String.valueOf(befYear) + "0" + String.valueOf(befMonth);
		} else {
			return String.valueOf(befYear) + String.valueOf(befMonth);
		}
		
	}
	
}
