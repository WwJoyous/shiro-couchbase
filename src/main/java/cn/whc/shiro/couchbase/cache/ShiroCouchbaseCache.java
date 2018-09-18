package cn.whc.shiro.couchbase.cache;

import cn.whc.shiro.couchbase.utils.SerializeUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.*;

/**
 * @author: whc
 * @Description:
 * @Date: Create in 16:40 2018/9/18
 */
@Slf4j
public class ShiroCouchbaseCache<K, V> implements Cache<K, V> {

    private Bucket bucket;

    private String bucketName;

    private String cachePrefix = "c_shiro";

    /**
     * unit second,default half hour
     */
    private int expire = 1800;

    public ShiroCouchbaseCache(Bucket bucket, String bucketName) {
        this.bucket = bucket;
        this.bucketName = bucketName;
    }

    public ShiroCouchbaseCache(Bucket bucket, String bucketName, int expire) {
        this(bucket, bucketName);
        this.expire = expire;
    }

    public ShiroCouchbaseCache(Bucket bucket, String bucketName, int expire, String cachePrefix) {
        this(bucket, bucketName, expire);
        this.cachePrefix = cachePrefix;
    }

    /**
     * @param k Object
     * @return AuthorizationInfo
     * @throws CacheException
     */
    @Override
    public V get(K k) throws CacheException {
        try {
            JsonStringDocument key = JsonStringDocument.create(getCouchbaseCacheKey(k));
            JsonStringDocument value = bucket.get(key);
            if (value != null) {
                return SerializeUtils.deserializeFromString(value.content());
            }
        } catch (Exception e) {
            log.error("shiro get couchbase cache error:{}", e);
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        try {
            String key = getCouchbaseCacheKey(k);
            String value = SerializeUtils.serializeToString(k);
            JsonStringDocument jsonStringDocument = JsonStringDocument.create(key, expire, value);
            bucket.upsert(jsonStringDocument);
        } catch (Exception e) {
            log.error("shiro put couchbase cache error:{}", e);
        }
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        V v = null;
        try {
            JsonStringDocument jsonStringDocument = JsonStringDocument.create(getCouchbaseCacheKey(k));
            JsonStringDocument value = bucket.get(jsonStringDocument);
            if (value == null) {
                return null;
            }
            v = SerializeUtils.deserializeFromString(value.content());
            bucket.remove(jsonStringDocument);
        } catch (Exception e) {
            log.error("shiro remove couchbase cache error:{}", e);
        }
        return v;
    }

    @Override
    public void clear() throws CacheException {
        try {
            N1qlQueryResult result = bucket.query(N1qlQuery.simple("DELETE FROM `" + bucketName + "` where META().id like '" + cachePrefix + "%'"));
            log.info("shiro clear couchbase count:{}", result.info().mutationCount());
        } catch (Exception e) {
            log.error("shiro clear couchbase cache error:{}", e);
        }
    }

    @Override
    public int size() {
        long size = 0;
        try {
            N1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT * FROM `" + bucketName + "` where META().id like '" + cachePrefix + "%'"));
            size = result.info().resultSize();
        } catch (Exception e) {
            log.error("shiro size couchbase cache error:{}", e);
        }
        return (int) size;
    }

    @Override
    public Set<K> keys() {
        Set set = new HashSet();
        try {
            N1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT META().id FROM `" + bucketName + "` where META().id like '" + cachePrefix + "%'"));
            List<N1qlQueryRow> rows = result.allRows();
            for (N1qlQueryRow row : rows) {
                set.add(row.value().getObject("id"));
            }
        } catch (Exception e) {
            log.error("shiro keys couchbase cache error:{}", e);
        }
        return null;
    }

    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<>();
        try {
            N1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT * FROM `" + bucketName + "` where META().id like '" + cachePrefix + "%'"));
            List<N1qlQueryRow> rows = result.allRows();
            for (N1qlQueryRow row : rows) {
                V v = SerializeUtils.deserializeFromString(row.value().getString(bucketName));
                list.add(v);
            }
        } catch (Exception e) {
            log.error("shiro values couchbase cache error:{}", e);
        }
        return list;
    }

    private String getCouchbaseCacheKey(K k) {
        return this.cachePrefix + k.toString();
    }
}
