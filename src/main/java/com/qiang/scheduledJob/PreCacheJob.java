package com.qiang.scheduledJob;

import cn.hutool.json.JSONUtil;
import com.qiang.domain.DTO.UserDTO;
import com.qiang.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.qiang.constant.UserConstant.*;

@Component
@Slf4j
public class PreCacheJob {
    private Long userId = 1744328L;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    //@Scheduled(cron = "0  0/4 * * ?") //每四个小时更新一次
    @Scheduled(cron = "0 0/1 * * * ?") //每20秒更新一次
    public void doCacheRecommend(){
        //设置锁
        RLock lock = redissonClient.getLock(LOCK_RECOMMEND_PRECACHE_SCHEDULED);
        try{
            //获取锁
            if (lock.tryLock(0,-1, TimeUnit.SECONDS)){//如果获取锁成功，就执行业务逻辑
                log.debug(Thread.currentThread().getId()+"获取锁成功，开始执行定时任务");
                //获取用户id
                String key = PRECACHE_RECOMMEND_USER+userId;
                //查询用户的标签列表
                String tagNameStr = userMapper.getTagNameByUserId(userId);
                //根据标签列表获取相关用户
                List<String> tagNameList = JSONUtil.toList(tagNameStr,String.class);
                List<UserDTO> userDTOS = userMapper.getuserByAnyTagnamePageWithCount(tagNameList, PRECACHE_RECOMMEND_USER_COUNT);
                //更新缓存
                stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(userDTOS));
                stringRedisTemplate.expire(key,PRECACHE_RECOMMEND_USER_COUNT, TimeUnit.MINUTES);//设置有效时间
            }
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }finally {
            log.debug(Thread.currentThread().getId()+"开始释放锁！");
            if (lock.isHeldByCurrentThread()) {//如果是当前线程拿到的锁，就释放锁
                lock.unlock();//内部是原子性的，判断锁和释放锁由lua脚本保证原子性
            }
        }
    }
}
