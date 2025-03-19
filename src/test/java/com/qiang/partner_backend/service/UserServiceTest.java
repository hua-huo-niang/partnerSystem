package com.qiang.partner_backend.service;

import cn.hutool.core.date.StopWatch;
import com.alibaba.excel.EasyExcel;
import com.qiang.domain.entity.Result;
import com.qiang.domain.entity.User;
import com.qiang.service.InsertService;
import com.qiang.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceTest{

    @Autowired
    private UserService userService;

    @Autowired
    private InsertService insertService;

    @Test
    void testSearchUserByTagNameList(){
        List<String> str = Arrays.asList("java","python");
        Result result = userService.getUsersByTagName(str);
        System.out.println(result);

    }


//    @Test
    void testBatchInsert(){
        List<User> data = new ArrayList<>();
        User user = new User();
        user.setUsername("假用户");
        user.setUserAccount("123456789");
        user.setUserPassword("$2a$10$GMdH992FLB7RLf4wCNclYu3Qh6C4DvntONcXS63o7DN7ksKN2st3K");
        user.setAvatarUrl("https://tse1-mm.cn.bing.net/th/id/OIP-C.Knh5i_ceDHm_cwzEcKFJ2gAAAA?w=164&h=180&c=7&r=0&o=5&pid=1.7");
        user.setGender(1);
        user.setPhone("138541685");
        user.setEmail("310@qq.com");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("1121");
        user.setTags("[\"java\", \"C++\", \"JS\", \"男\"]");
        user.setProfile("我是一个假的用户，仅用于测试使用！");
        for (int i = 0;i<10000;i++){
            data.add(user);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        insertService.batchInsertUsers(data, 1000);
        stopWatch.stop();
        System.out.println("成功导入数据！耗时："+stopWatch.getTotalTimeMillis()+"ms");
    }

//    @Test
    void testOneInsert(){
        User user = new User();
        user.setUsername("假假用户");
        user.setUserAccount("123456789");
        user.setUserPassword("$2a$10$GMdH992FLB7RLf4wCNclYu3Qh6C4DvntONcXS63o7DN7ksKN2st3K");
        user.setAvatarUrl("https://tse1-mm.cn.bing.net/th/id/OIP-C.Knh5i_ceDHm_cwzEcKFJ2gAAAA?w=164&h=180&c=7&r=0&o=5&pid=1.7");
        user.setGender(1);
        user.setPhone("138541685");
        user.setEmail("310@qq.com");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("1121");
        user.setTags("[\"java\", \"C++\", \"JS\", \"男\"]");
        user.setProfile("我是一个假假的用户，仅用于测试使用！");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0;i<1;i++){
            userService.addOneUser(user);
        }
        stopWatch.stop();
        System.out.println("成功导入数据！耗时："+stopWatch.getTotalTimeMillis()+"ms");
    }

//    @Test
    void testMassiveBatchInsert(){
        List<User> data = new ArrayList<>();
        User user = new User();
        user.setUsername("假假用户");
        user.setUserAccount("123456789");
        user.setUserPassword("$2a$10$GMdH992FLB7RLf4wCNclYu3Qh6C4DvntONcXS63o7DN7ksKN2st3K");
        user.setAvatarUrl("https://tse1-mm.cn.bing.net/th/id/OIP-C.Knh5i_ceDHm_cwzEcKFJ2gAAAA?w=164&h=180&c=7&r=0&o=5&pid=1.7");
        user.setGender(1);
        user.setPhone("138541685");
        user.setEmail("310@qq.com");
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("1121");
        user.setTags("[\"java\", \"C++\", \"JS\", \"男\"]");
        user.setProfile("我是一个假假的用户，仅用于测试使用！");
        for (int i = 0;i<5000;i++){
            data.add(user);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        insertService.insertMassiveData(data, 6,100);
        stopWatch.stop();
        System.out.println("成功导入数据！耗时："+stopWatch.getTotalTimeMillis()+"ms");
    }

    /**
     * 填充excel表格
     */
    @Test
    void fillExcel(){
        String InputFile = "src/main/resources/prodExcel.xlsx";
        String outputFile = "src/main/resources/prodExcel3.xlsx";
        List<User> existingData = EasyExcel.read(InputFile).head(User.class).sheet("Sheet1").doReadSync();
        List<User> newData = new ArrayList<>();
        for (int i = 0;i<3000;i++){//循环3000次，原本有200条数据。一共600000条数据
            newData.addAll(existingData);
        }
        EasyExcel.write(outputFile,User.class).sheet("Sheet1").doWrite(newData);
    }

    @Test
    void excelReadAndBatchInsert(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        insertService.excelReadAndBatchInsert("src/main/resources/prodExcel3.xlsx",100000,20);
        stopWatch.stop();
        System.out.println("成功导入数据！耗时："+stopWatch.getTotalTimeMillis()+"ms");
    }


    /**
     * 与监听器多线程多批插入进行对比性能
     */
    @Test
    void excelReadAndInsert(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        insertService.excelReadAndBatchInsert("src/main/resources/testExcel.xlsx",100000,20);
        stopWatch.stop();
        System.out.println("成功导入数据！耗时："+stopWatch.getTotalTimeMillis()+"ms");

    }
}
