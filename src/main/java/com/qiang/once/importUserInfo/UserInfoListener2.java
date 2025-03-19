package com.qiang.once.importUserInfo;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.qiang.domain.entity.User;
import com.qiang.service.UserService;

/**
 * 普通的监听器
 * 作用：使用普通方式插入数据到数据库，与多线程多批方式进行对比
 */
public class UserInfoListener2 implements ReadListener<User> {

    private UserService userService;

    public UserInfoListener2(UserService userService){
        this.userService = userService;
    }

    @Override
    public void invoke(User user, AnalysisContext analysisContext) {
        userService.addOneUser(user);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
