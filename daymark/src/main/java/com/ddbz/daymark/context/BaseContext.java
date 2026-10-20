package com.ddbz.daymark.context;

public class BaseContext {
    private static ThreadLocal<String> loginThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Integer> roleThreadLocal = new ThreadLocal<>();

    public static void setCurrentUid(String uid) {loginThreadLocal.set(uid);}

    public static void setCurrentRole(int role){roleThreadLocal.set(role);}

    public static String getCurrentUid() {return loginThreadLocal.get();}

    public static int getCurrentRole() {return roleThreadLocal.get();}

    public static void clear() {
        loginThreadLocal.remove();
        roleThreadLocal.remove();
    }
}
