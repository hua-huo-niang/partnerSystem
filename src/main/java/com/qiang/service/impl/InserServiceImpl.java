package com.qiang.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.excel.EasyExcel;
import com.qiang.domain.entity.User;
import com.qiang.exception.BusinessException;
import com.qiang.mapper.UserMapper;
import com.qiang.once.importUserInfo.UserInfoListener;
import com.qiang.service.InsertService;
import com.qiang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.qiang.util.ErrorCode.ERROR_PARAMS;

@Service
@Slf4j
public class InserServiceImpl implements InsertService {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;



    /**
     * 批插入用户数据到数据库
     * 原理：
     *  1. 由自己控制事务的提交，使用缓存缓存多个sql语句，然后一次性发送到数据库。
     *  2. 减少网络往返次数，减少与数据库的连接次数
     * @param users  用户列表
     * @param batchSize  单批的大小，每一批中的数据量
     * @return Long 成功插入的条数
     */
    @Override
    public void batchInsertUsers(List<User> users, int batchSize) {

        if (users.isEmpty()){
            throw new BusinessException(ERROR_PARAMS,"参数错误，没有数据来进行插入！");
        }
        //不使用spring管理的SqlSessionTemplate，也就是不从连接池获取连接，而是自己创建一个，自己控制事务的提交
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)){
            //获取与当前sqlSession绑定的Mapper，使用srping管理的mapper可能会自动提交事务
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            int total = users.size();
            for (int i = 0; i < total; i+=batchSize) {
                //截取当前批次的数据
                List<User> batchList = users.subList(i, Math.min(i + batchSize, total));
                //执行批量插入
                System.out.println("正在插入数据中。。。。。");
                mapper.batchInsert(batchList);
                //每批提交一次，避免内存溢出
                System.out.println("当前的线程："+Thread.currentThread().getName());
                System.out.println("connection的信息："+sqlSession.getConnection());
                System.out.println("准备提交事务！");
                sqlSession.commit();
                //清理缓存
                sqlSession.clearCache();
            }
        } catch (Exception e) {
            log.error("批量插入异常");
            e.printStackTrace();
        }
    }




    /**
     * 使用多线程将一个海量的数据进行分片，然后使用多线程进行插入数据库
     * 每一条线程使用的是分批的操作
     * @param dataList 数据
     * @param threadPoolSize  线程的大小
     * @param batchSize 一批多大
     *
     */
    public void insertMassiveData(List<User> dataList,int threadPoolSize,int batchSize){
        if (dataList.isEmpty()) {
            throw new BusinessException(ERROR_PARAMS,"参数错误，没有数据！");
        }
        //将数据分片
        List<List<User>> shards = ListUtil.split(dataList, dataList.size() / threadPoolSize);
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        //倒计时
        CountDownLatch latch = new CountDownLatch(shards.size());
        for (int i = 0; i < shards.size(); i++) {
            int finalIndex = i;
            executorService.submit(()->{
                //获取连接
                Connection conn = DataSourceUtils.getConnection(dataSource);
                //开启事务管理
                try {
                    conn.setAutoCommit(false);
                    //数据分批
                    int total = shards.get(finalIndex).size();
                    for (int j = 0; j < total; j+=batchSize) {
                        List<User> usersList = shards.get(finalIndex).subList(j, Math.min(total, j + batchSize));
                        userMapper.batchInsert(usersList);
                        conn.commit();
                    }
                } catch (SQLException e) {
                    //处理异常情况
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    e.printStackTrace();
                }finally {
                    DataSourceUtils.releaseConnection(conn,dataSource);
                    latch.countDown();
                }
                System.out.println(Thread.currentThread().getName());
            },"t1");
        }
        //等待全部线程结束
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //关闭线程池
        executorService.shutdown();
    }

    /**
     * 读取excel文件中的数据，然后导入到数据库中
     * 方式：监听器监听excel行，然后使用多线程导入数据库中
     *
     * @param filePath excel 文件地址
     * @param threshold excel缓存阈值大小
     * @param threadPoolSize 线程池的大小
     */
    @Override
    public void excelReadAndBatchInsert(String filePath, Integer threshold, Integer threadPoolSize) {
        EasyExcel.read(filePath,User.class,new UserInfoListener(threshold,threadPoolSize,this))
//        EasyExcel.read(filePath,User.class,new UserInfoListener2(userService))
                .sheet()
                .doRead();
    }
}
