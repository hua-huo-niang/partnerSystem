package com.qiang.service;

import com.qiang.domain.BO.User;

import java.util.List;


public interface InsertService {

    void batchInsertUsers(List<User> users, int batchSize);


    void insertMassiveData(List<User> dataList,int threadPoolSize,int batchSize);

    void excelReadAndBatchInsert(String filePath,Integer threshold,Integer threadPoolSize);
}
