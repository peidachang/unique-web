package org.unique.web.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 全局的WeContext
 * @author biezhi
 * @since 1.0
 */
public final class WebContext {

    private static final ThreadLocal<WebContext> actionContextThreadLocal = new ThreadLocal<WebContext>();
    
    private ServletContext context;
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    /**
     * get ServletContext
     */
    public static ServletContext getServletContext() {
        return single().context;
    }

    /**
     * get Request
     */
    public static HttpServletRequest getHttpServletRequest() {
        return single().request;
    }

    /**
     * get Response
     */
    public static HttpServletResponse getHttpServletResponse() {
        return single().response;
    }

    /**
     * get HttpSession
     */
    public static HttpSession getHttpSession() {
        return single().request.getSession();
    }

    /**
     * get ActionContext
     */
    private static WebContext single() {
        WebContext actionContext = actionContextThreadLocal.get();
        if(null != actionContext){
            return actionContext;
        }
        actionContextThreadLocal.set(new WebContext());
        return actionContextThreadLocal.get();
    }
    
    public static void remove(){
    	actionContextThreadLocal.remove();
    }
    
    public static String getContextPath(){
        return single().context.getContextPath();
    }
    
    /**
     * 设置WebUtil的根路径
     * @param context ServletContext对象
     */
    public static void setActionContext(ServletContext context){
        single().context = context;
    }
    
    /**
     * 设置context对象到ActionContext中
     * @param context ServletContext对象
     * @param request HttpServletRequest对象
     * @param response HttpServletResponse对象
     */
    public static void setActionContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
    	WebContext actionContext = single();
    	actionContext.context = context;
    	actionContext.request = request;
    	actionContext.response = response;
    }

    /**
     * 移除ActionContext
     */
    static void removeActionContext() {
        actionContextThreadLocal.remove();
    }
}

