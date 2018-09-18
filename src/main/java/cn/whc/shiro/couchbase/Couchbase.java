package cn.whc.shiro.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;

/**
 * @author: whc
 * @Description:
 * @Date: Create in 17:27 2018/9/18
 */
public class Couchbase {

    /**
     * init couchbase demo
     *
     * @param args
     */
    public static void main(String[] args) {
//        CouchbaseCluster couchbaseCluster = CouchbaseCluster.create("172.26.9.13", "172.26.9.14");
        CouchbaseCluster couchbaseCluster = CouchbaseCluster.create(); //default 127.0.0.1
        String username = "admin";
        String password = "123456";
        couchbaseCluster.authenticate(username, password);
        String bucketName = "default";
        int ramQuota = 100;//default
        initBucket(couchbaseCluster, bucketName, ramQuota);
    }

    static Bucket initBucket(CouchbaseCluster couchbaseCluster, String bucketName, int ramQuota) {
        ClusterManager clusterManager = couchbaseCluster.clusterManager();
        if (!clusterManager.hasBucket(bucketName)) {

            BucketSettings bucketSettings = new DefaultBucketSettings.Builder().
                    type(BucketType.COUCHBASE).
                    name(bucketName).
                    quota(ramQuota > 0 ? ramQuota : 100).
                    enableFlush(true).
                    build();
            clusterManager.insertBucket(bucketSettings);
        }
        Bucket bucket = couchbaseCluster.openBucket(bucketName);
        bucket.bucketManager().createN1qlPrimaryIndex(true, false); //this is required

//        bucket.bucketManager().flush();
        return bucket;
    }
}
