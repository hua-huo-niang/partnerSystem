package com.qiang.constant;

public class UserConstant {
    //同一账号申请验证码次数
    public static final String CAPTCHA_COUNT = "captcha:count:";
    //同一账号在规定时间内的最多申请
    public static final Long CAPTCHA_COUNT_TTL = 10L;

    //注册验证码key
    public  static final String REGIST_CODE = "REGIST:CODE:";
    //注册验证码key过期时间
    public static final Long REGIST_CODE_TTL = 2L;

    //登录token
    public static final String LOGIN_TOKEN = "LOGIN:TOKEN:";
    //登录token过期时间
    public static final Long LOGIN_TOKEN_TTL = 30L;

    //允许的登录错误次数
    public static final Integer ALLOWABLE_ERROR_COUNT = 10;
    //登录错误次数
    public static final String LOGIN_FAIL = "LOGIN:FAIL:";
    //登录试错冷却
    public static final Long LOGIN_FAIL_TTL = 10L;

    //用户权限
    public static final Integer ADMINISTRATOR_AUTHORITY = 1;
    public static final Integer DOMESTIC_AUTHORITY = 0;
}
