package com.qiang.once.importUserInfo;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.qiang.domain.entity.User;
import com.qiang.service.InsertService;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// 有个很重要的点 DemoDataListener 不能被spring管理，
// 要每次读取excel都要new,然后里面用到spring可以构造方法传进去
@Slf4j
public class UserInfoListener implements ReadListener<User> {
    /**
     * 每一批次的大小
     * 默认值是10000
     */
    private Integer threshold = 10000;
    /**
     * 线程池的大小
     * 默认是6
     */
    private Integer threadPoolSize = 6;//默认线程池大小为6
    /**
     * 每一次读取excel的缓存存放列表
     */
    private List<User> cacheDataList = new ArrayList<>();
    /**
     * Mapper
     * 通过构造方法初始化
     */
    private final InsertService insertService;

    /**
     * 线程池
     * 通过构造方法进行初始化
     */
    private final ExecutorService executorService;

    /**
     * 构造方法
     * @param threshold 每批次的大小
     * @param threadPoolSize  线程池的大小
     * @param insertService  InsertService
     */
    public UserInfoListener(Integer threshold, Integer threadPoolSize, InsertService insertService){
        this.threshold = threshold;
        this.threadPoolSize = threadPoolSize;
        this.insertService = insertService;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }


    /**
     * 这个每一条数据解析都会来调用
     *  1. 处理每一行读取到的信息，放入到缓存中，
     *  2. 当缓存列表满了，就创建线程使用分批处理去处理
     * @param data 一条数据
     * @param context
     */
    @Override
    public void invoke(User data, AnalysisContext context) {
        //缓存每一行读取到的数据
        cacheDataList.add(data);
        //当达到缓存的阈值，就使用mapper调用数据库导入进行导入
        if (cacheDataList.size()>threshold){
            ArrayList<User> batch = new ArrayList<>(cacheDataList);
            //清空缓存，继续接受数据
            cacheDataList.clear();
            executorService.submit(()->{insertService.batchInsertUsers(batch,threshold/10);},"excelThread-");
        }
    }



    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!cacheDataList.isEmpty()) {
            executorService.submit(()->{insertService.batchInsertUsers(cacheDataList,threshold/10);});
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                log.warn("任务执行超时！");
            }
        } catch (InterruptedException e) {
            log.error("线程等待异常", e);
        }
    }
}
