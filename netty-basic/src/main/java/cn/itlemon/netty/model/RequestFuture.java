package cn.itlemon.netty.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-24
 */
public class RequestFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestFuture.class);
    private static final long TIMEOUT = 5000L;

    /**
     * 请求缓存类，key为每次请求id，value为请求对象
     */
    public static Map<Long, RequestFuture> futures = new ConcurrentHashMap<>();

    private static final AtomicLong A_ID = new AtomicLong(1);

    private long id;
    private Object request;
    private Object result;

    public RequestFuture() {
        this.id = A_ID.incrementAndGet();
        addFuture(this);
    }

    /**
     * 将请求加入缓存中
     *
     * @param future 请求
     */
    public static void addFuture(RequestFuture future) {
        futures.put(future.getId(), future);
    }

    /**
     * 获取同步响应结果
     *
     * @return 结果
     */
    public Object get() {
        synchronized (this) {
            while (result == null) {
                try {
                    this.wait(TIMEOUT);
                } catch (Exception e) {
                    LOGGER.error("get sync result fail.", e);
                }
            }
        }
        return result;
    }

    /**
     * 异步线程将结果返回给主线程
     *
     * @param response 请求结果
     */
    public static void received(Response response) {
        RequestFuture future = futures.remove(response.getId());
        if (future != null) {
            future.setResult(response.getResult());
        }
        synchronized (Objects.requireNonNull(future)) {
            future.notify();
        }

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
