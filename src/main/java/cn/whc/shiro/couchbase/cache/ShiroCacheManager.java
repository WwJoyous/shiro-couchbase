package cn.whc.shiro.couchbase.cache;

import com.couchbase.client.java.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: whc
 * @Description:
 * @Date: Create in 16:38 2018/9/18
 */
@Slf4j
public class ShiroCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    private Bucket bucket;

    private String bucketName;

    /**
     * unit second,default half hour
     */
    private int expire = 1800;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        log.info("get cache name:{}", name);
        Cache cache = caches.get(name);
        if (cache == null) {
            cache = new ShiroCouchbaseCache(bucket, bucketName, expire);
            caches.put(name, cache);
        }
        return cache;
    }
}
