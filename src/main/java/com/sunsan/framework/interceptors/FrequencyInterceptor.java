package com.sunsan.framework.interceptors;


import com.sunsan.framework.annotation.ApiFrequency;
import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import com.sunsan.framework.model.ErrorResp;
import com.sunsan.framework.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;


@Component
@ApiFrequency(name = "default", time = 1, limit = 5) //限制验证接口访问频率，默认一秒5次
public class FrequencyInterceptor extends HandlerInterceptorAdapter {
    private static final int MAX_BASE_STATION_SIZE = 100000;
    private static final float SCALE = 0.75F;
    private static final int MAX_CLEANUP_COUNT = 3;
    private static final int CLEANUP_INTERVAL = 1000;
    private static Map<String, FrequencyInfo> BASE_STATION = new HashMap<>(MAX_BASE_STATION_SIZE);
    private final Object syncRoot = new Object();
    private Logger logger = LoggerFactory.getLogger(FrequencyInterceptor.class);
    private int cleanupCount = 0;
    private ApiFrequency defaultFrequency = getClass().getAnnotation(ApiFrequency.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if (request.getRequestURI().startsWith("/error"))
            return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        ApiFrequency methodApiFrequency = handlerMethod.getMethodAnnotation(ApiFrequency.class);
        ApiFrequency classApiFrequency = handlerMethod.getBean().getClass().getAnnotation(ApiFrequency.class);

        boolean going = true;
        String methodName = method.getName();

        if (methodApiFrequency != null) {
            going = handleFrequency(request, response, methodApiFrequency, methodName);
        } else {
            if (classApiFrequency != null) {
                going = handleFrequency(request, response, classApiFrequency, methodName);
            } else {
                // 默认限制是一秒5次
                going = handleFrequency(request, response, defaultFrequency, methodName);
            }
        }

        if (!going) {
            ErrorResp.respErrorApi(response, new ApiException(ErrCode.frequency), HttpServletResponse.SC_BAD_REQUEST);
        }

        return going;
    }

    private boolean handleFrequency(HttpServletRequest request, HttpServletResponse response, ApiFrequency frequency, String methodName) {

        boolean going = true;
        if (frequency == null) {
            return true;
        }

        String name = frequency.name();
        int limit = frequency.limit();
        int time = frequency.time();

        System.out.println(String.format("%s %s %s %s", name, methodName, limit, time));

        if (time == 0 || limit == 0) {
            return false;
        }

        long currentTimeMilles = System.currentTimeMillis();
        String ip = WebUtils.getRemoteAddr(request);
        // System.out.println("ip = [" + ip + "]");
        if (StringUtils.isBlank(ip)) {
            return false;
        }

        String key = ip + "_" + name + "_" + methodName;
        FrequencyInfo frequencyInfo = BASE_STATION.get(key);

        if (frequencyInfo == null) {

            frequencyInfo = new FrequencyInfo();
            frequencyInfo.uniqueKey = name;
            frequencyInfo.start = frequencyInfo.end = currentTimeMilles;
            frequencyInfo.limit = limit;
            frequencyInfo.time = time;
            frequencyInfo.accessPoints.add(currentTimeMilles);

            synchronized (syncRoot) {
                BASE_STATION.put(key, frequencyInfo);
            }
            if (BASE_STATION.size() > MAX_BASE_STATION_SIZE * SCALE) {
                cleanup(currentTimeMilles);
            }
        } else {

            frequencyInfo.end = currentTimeMilles;
            frequencyInfo.accessPoints.add(currentTimeMilles);
        }

        // 时间是否有效
        if (frequencyInfo.end - frequencyInfo.start >= time * 1000) {
            // logger.info("Frequency info be out of date, info will be reset., info: {}", frequencyInfo.toString());
            frequencyInfo.reset(currentTimeMilles);
        } else {
            int count = frequencyInfo.accessPoints.size();
            if (count > limit) {
                logger.info("key: {} too Frequency. count: {}, limit: {}.", key, count, limit);
                going = false;
            }
        }
        return going;
    }

    private void cleanup(long currentTimeMilles) {

        synchronized (syncRoot) {

            Iterator<String> it = BASE_STATION.keySet().iterator();
            while (it.hasNext()) {

                String key = it.next();
                FrequencyInfo freqStruct = BASE_STATION.get(key);
                if ((currentTimeMilles - freqStruct.end) > freqStruct.time * 1000) {
                    it.remove();
                }
            }

            if ((MAX_BASE_STATION_SIZE - BASE_STATION.size()) > CLEANUP_INTERVAL) {
                cleanupCount = 0;
            } else {
                cleanupCount++;
            }

            if (cleanupCount > MAX_CLEANUP_COUNT) {
                randomCleanup(MAX_CLEANUP_COUNT);
            }
        }
    }

    /**
     * 随机淘汰count个key
     *
     * @param count
     */
    private void randomCleanup(int count) {
        //防止调用错误
        if (BASE_STATION.size() < MAX_BASE_STATION_SIZE * SCALE) {
            return;
        }

        Iterator<String> it = BASE_STATION.keySet().iterator();
        Random random = new Random();
        int tempCount = 0;

        while (it.hasNext()) {
            if (random.nextBoolean()) {
                it.remove();
                tempCount++;
                if (tempCount >= count) {
                    break;
                }
            }
        }
    }

    public class FrequencyInfo {
        String uniqueKey;

        //单位毫秒
        long start;

        //单位毫秒
        long end;

        // 限制两次访问的时间间隔,单位秒
        int time;

        // 限制同一个ip下，时间间隔中可以访问的次数
        int limit;

        List<Long> accessPoints = new ArrayList<Long>();

        public void reset(long timeMillis) {

            start = end = timeMillis;
            accessPoints.clear();
            accessPoints.add(timeMillis);
        }

        @Override
        public String toString() {
            return "FrequencyInfo [uniqueKey=" + uniqueKey + ", start=" + start
                    + ", end=" + end + ", time=" + time + ", limit=" + limit
                    + ", accessPoints=" + accessPoints + "]";
        }
    }
}
