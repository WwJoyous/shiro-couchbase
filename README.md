# shiro-couchbase

#### shiro session 和缓存同步到couchbase中
##### 重写shiro cache
##### 重新实现了shiro的SessionDao
##### 整合到shiro中
##### 需要把ShiroSessionDao配置ShiroCacheManager中的sessionDao中
##### 需要把ShiroCacheManager配置到shiro DefaultWebSecurityManager
