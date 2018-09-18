package cn.whc.shiro.couchbase.session;

import cn.whc.shiro.couchbase.utils.SerializeUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: whc
 * @Description: use to setting configuration shiro session
 * @Date: Create in 16:04 2018/9/18
 */
@Slf4j
public class ShiroSessionDao extends AbstractSessionDAO {

    private String keyPrefix = "c_shiro_session";

    /**
     * unit second,default half hour
     */
    private int expire = 1800;

    /**
     * used to '@Autowired' the Bucket
     */
    private Bucket bucket;

    /**
     * used to set the bucket name
     */
    private String bucketName;

    @Override
    protected Serializable doCreate(Session session) {
        if (session == null) {
            log.error("session is null");
            throw new UnknownSessionException("session is null");
        }
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        this.saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            log.warn("session id is null");
            return null;
        }
        Session session = null;
        try {
            String key = getCouchbaseSessionKey(sessionId);
            JsonStringDocument jsonStringDocument = JsonStringDocument.create(key);
            JsonStringDocument value = bucket.get(jsonStringDocument);
            if (value != null) {
                session = SerializeUtils.deserializeFromString(value.content());
            } else {
                log.error("read session from couchbase is null");
            }
        } catch (Exception e) {
            log.error("do read session error:", e);
        }
        return session;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        log.info("shiro session update:{}", session);
        this.saveSession(session);
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            log.error("session or sessionId is null");
            return;
        }
        String key = getCouchbaseSessionKey(session.getId());
        JsonStringDocument jsonStringDocument = JsonStringDocument.create(key);
        bucket.remove(jsonStringDocument);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<Session> sessions = new HashSet<>();
        try {
            N1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT * FROM `" + bucketName + "` where META().id like '" + keyPrefix + "%'"));
            List<N1qlQueryRow> rows = result.allRows();
            for (N1qlQueryRow row : rows) {
                Session session = SerializeUtils.deserializeFromString(row.value().getString(bucketName));
                sessions.add(session);
            }
        } catch (Exception e) {
            log.error("get active sessions form couchbase error:{}", e);
        }
        return sessions;
    }

    private void saveSession(Session session) throws UnknownSessionException {
        if (session == null || session.getId() == null) {
            log.error("session or session id is null");
            throw new UnknownSessionException("session or session id is null");
        }
        try {
            String key = getCouchbaseSessionKey(session.getId());
            String value = SerializeUtils.serializeToString(session);
            JsonStringDocument jsonStringDocument = JsonStringDocument.create(key, expire, value);
            bucket.upsert(jsonStringDocument);
        } catch (Exception e) {
            throw new UnknownSessionException("save session to couchbase error:", e);
        }
    }

    private String getCouchbaseSessionKey(Serializable sessionId) {
        return this.keyPrefix + sessionId;
    }
}
