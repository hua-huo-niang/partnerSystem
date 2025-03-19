package com.qiang.util;

import com.qiang.domain.entity.UserHolderEntity;

/**
 * UserHolder类的作用是：
 *  在用户发送的请求线程中，保存当前用户的基本标识信息
 *  在后续业务中，提供快速的身份标识，如：删除信息的时候，快速获取身份权限，知道有没有权限，不用去redis或数据中获取
 */
public class UserHolder {
    private static final ThreadLocal<UserHolderEntity> tl = new ThreadLocal<>();

    public static void saveUser(UserHolderEntity user){
        tl.set(user);
    }

    public static UserHolderEntity getUser(){return tl.get();}

    public static void removeUser(){tl.remove();}
}
