package com.lzh.util;


import com.lzh.context.StuContext;

/**
 * 全局上下文透传工具
 */
public class ThreadLocalUtil {
    private static ThreadLocal<StuContext> threadLocal = new ThreadLocal<>();

    public static StuContext get(){
        return threadLocal.get();
    }

    public static void set(StuContext context){
        threadLocal.set(context);
    }

    public static void remove(){
        threadLocal.remove();
    }

    public static Long getId(){
        return threadLocal.get().stuId();
    }

}