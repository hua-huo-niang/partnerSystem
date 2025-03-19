package com.qiang.util;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * lua脚本执行工具类
 * 作用：
 *  类加载的时候，加载Rescources下的lua脚本进行封装成java类
 *  封装lua的执行方法
 */
public class LuaUtil {
    private static final DefaultRedisScript<Long> UPDATE_SCRIPT;
    static {
        UPDATE_SCRIPT = new DefaultRedisScript<>();
        UPDATE_SCRIPT.setLocation(new ClassPathResource("lua/update.lua"));
        UPDATE_SCRIPT.setResultType(Long.class);
    }
    static public Long updateUser(StringRedisTemplate stringRedisTemplate, List<String> key,List<String> args){
        //执行脚本语句
        Long count = stringRedisTemplate.execute(UPDATE_SCRIPT, key, args.toArray());
        return count;
    }
}
