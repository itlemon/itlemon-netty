package cn.itlemon.netty.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.itlemon.netty.model.RequestFuture;
import cn.itlemon.netty.model.Response;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-24
 */
public class SubThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubThread.class);

    private RequestFuture requestFuture;

    public SubThread(RequestFuture requestFuture) {
        this.requestFuture = requestFuture;
    }

    @Override
    public void run() {
        Response response = new Response();
        response.setId(requestFuture.getId());
        response.setResult("server response " + Thread.currentThread().getId());
        try {
            Thread.sleep(1000L);
        } catch (Exception e) {
            LOGGER.error("thread sleep fail.", e);
        }
        RequestFuture.received(response);
    }
}
