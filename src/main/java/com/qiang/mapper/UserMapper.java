package com.qiang.mapper;

import com.qiang.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User getOne(Integer id);

    Integer addOne(User user);

    Integer findOnRegist(@Param("userAccount") String userAccount);

    Integer addOnRegist(@Param("userAccount") String userAccount, @Param("userPassword") String userPassword);

    Long getIdByUserAccount(@Param("userAccount") String userAccount);

    User getUserByAccount(@Param("userAccount") String userAccount);

    Integer deleteOne(@Param("id") Integer id);
}
