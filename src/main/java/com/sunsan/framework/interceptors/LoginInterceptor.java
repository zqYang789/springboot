package com.sunsan.framework.interceptors;


import com.sunsan.framework.annotation.ApiPermissions;
import com.sunsan.project.entity.User;
import com.sunsan.framework.manager.TokenManager;
import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import com.sunsan.framework.model.ErrorResp;
import com.sunsan.framework.model.TokenUser;
import com.sunsan.project.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private TokenManager manager;

    @Autowired
    private UserService userService;


    // 不需要拦截的资源
    private List<String> excludeUrls = Arrays.asList("doc", "error");

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }

    private void checkTokenUser(TokenUser tokenUser, HttpServletRequest request) throws ApiException {
        //检查ip变化
        // String lastLoginIp = WebUtils.getRemoteAddr(request);
        // if (!tokenUser.getLastIp().equals(lastLoginIp))
        //     throw new ApiException(ErrCode.tokenIpChange);

        //检测设备变化
        //String lastDevice = DeviceUtils.getCurrentDevice(request).getDevicePlatform().name();
        //if (!tokenUser.getDevice().equals(lastDevice))
        //throw new ApiException(ErrCode.tokenDeviceChange);
    }

    /**
     * 判断是否有 authorizations = {@Authorization(value = "api_key"这个注解
     *
     * @param anns
     * @return
     */
    private boolean isHaveAdminAuthorization(Authorization[] anns) {
        for (Authorization ann : anns) {
            if (ann.value().equals("api_key"))
                return true;
        }
        return false;
    }

    /**
     * 这个方法的主要作用是用于清理资源的，当然这个方法也只能在当前这个Interceptor的preHandle方法的返回值为true时才会执行。
     */
    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {
        // TODO Auto-generated method stub

    }

    /**
     * 在Controller的方法调用之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object arg2, ModelAndView arg3)
            throws Exception {

        if (request.getAttribute("currentUser") != null)
            request.removeAttribute("currentUser");
        if (request.getAttribute("currentUserId") != null)
            request.removeAttribute("currentUserId");
    }


    /**
     * preHandle方法是进行处理器拦截用的，顾名思义，该方法将在Controller处理之前进行调用，
     * SpringMVC中的Interceptor拦截器是链式的，可以同时存在多个Interceptor，
     * 然后SpringMVC会根据声明的前后顺序一个接一个的执行，而且所有的Interceptor中的preHandle方法都会在Controller方法调用之前调用。
     * SpringMVC的这种Interceptor链式结构也是可以进行中断的，这种中断方式是令preHandle的返
     * 回值为false，当preHandle的返回值为false的时候整个请求就结束了。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = requestURI.substring(contextPath.length() + 1);


        if (url.startsWith("swagger") || excludeUrls.contains(url)) {
            return true;
        }

        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();
        Object bean = handlerMethod.getBean();

        try {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            if (apiOperation == null) {
                throw new ApiException(ErrCode.noApiOperation);
            }

            // 根据注解判断是否需要验证
            Authorization[] anns = apiOperation.authorizations();
            boolean adminAuth = isHaveAdminAuthorization(anns);
            if (!adminAuth) {
                // 虽然有些接口无须验证，但是它也传递了api_key,也做出相应的处理
                // 有异常也无须反馈给前端
                try {
                    String token = request.getHeader("api_key");

                    if (isTestToken(request, token))
                        return true;

                    if (StringUtils.isNotEmpty(token)) {
                        TokenUser tokenUser = manager.getTokenUserFromToken(token);
                        Integer userId = tokenUser.getUserId();
                        //保存全局用户信息
                        request.setAttribute("currentUserId",userId);
                    }
                } catch (Exception ignored) {
                }

                return true;
            }

            //后台登录检测
            String[] perms = getApiPerms(method, beanType);
            checkAdminWebTokenAndPerm(request, perms);

            return true;
        } catch (ApiException e) {
            int responseStatus = HttpServletResponse.SC_UNAUTHORIZED;
            int[] internalEcs = new int[]{
                    ErrCode.noApiOperation.getCode(),
                    ErrCode.noPermission.getCode(),
                    ErrCode.invalidAuthCode.getCode(),
                    ErrCode.emptyAuthCode.getCode(),
                    ErrCode.errAuthCode.getCode(),
            };
            if (ArrayUtils.contains(internalEcs, e.getCode()))
                responseStatus = HttpServletResponse.SC_BAD_REQUEST;
            ErrorResp.respErrorApi(response, e, responseStatus);
            return false;
        }
    }



    private String[] getApiPerms(Method method, Class<?> beanType) {
        Set<String> permsSet = new HashSet<>();

        //处理ApiPermissions注解
        ApiPermissions apiPermission = method.getAnnotation(ApiPermissions.class);
        if (apiPermission == null)
            apiPermission = beanType.getAnnotation(ApiPermissions.class);
        if (apiPermission != null)
            permsSet.addAll(Arrays.asList(apiPermission.value()));

        /*

        //处理ApiRolesPerm注解
        ApiRolesPerm apiRolesPerm = method.getAnnotation(ApiRolesPerm.class);
        if (apiRolesPerm == null)
            apiRolesPerm = beanType.getAnnotation(ApiRolesPerm.class);
        if (apiRolesPerm != null)
            permsSet.addAll(userService.getPermsByRoles(apiRolesPerm.value()));

        //处理ApiUsersPerm 注解
        ApiUsersPerm apiUsersPerm = method.getAnnotation(ApiUsersPerm.class);
        if (apiUsersPerm == null)
            apiUsersPerm = beanType.getAnnotation(ApiUsersPerm.class);
        if (apiUsersPerm != null)
            permsSet.addAll(userService.getPermsByUsers(apiUsersPerm.value()));

        String[] perms = new String[permsSet.size()];
        return permsSet.toArray(perms);
        */
        return null;
    }



    private boolean isTestToken(HttpServletRequest request, String token) {
        if (StringUtils.equals(token, "888888")) {
            User user = userService.getTestUser();
            request.setAttribute("currentUser", user);
            request.setAttribute("currentUserId", user.getUserid());
            return true;
        }
        return false;
    }

    private void checkAdminWebTokenAndPerm(HttpServletRequest request, String[] perms) throws Exception {
        //后台登录检测
        String token = request.getHeader("api_key");
        // 获取api_key,即token,token的所有失败情况由异常处理
        if (StringUtils.isEmpty(token)) {
            throw new ApiException(ErrCode.nullToken);
        }

        if (isTestToken(request, token))
            return;

        // 判断token本身和redis的token是否有效
        TokenUser tokenUser = manager.getTokenUserFromToken(token);
        Integer userId = tokenUser.getUserId();

        checkTokenUser(tokenUser, request);
        // 更新redis token失效时间
        manager.refreshToken(tokenUser);

        /*
        // 权限检查
        if (perms != null && perms.length > 0) {
            for (String perm : perms) {
                if (!userService.havePermission(userId, perm))
                    throw new ApiException(ErrCode.noPermission);
            }
        }

        */
        //保存全局用户信息
        request.setAttribute("currentUserId",userId);
    }

}
