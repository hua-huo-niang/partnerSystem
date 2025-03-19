package com.qiang.mapper;

import com.qiang.domain.DTO.UserDTO;
import com.qiang.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    UserDTO getOne(Long id);

    Integer addOne(User user);

    Integer findOnRegist(@Param("userAccount") String userAccount);

    Integer addOnRegist(@Param("userAccount") String userAccount, @Param("userPassword") String userPassword);

    Long getIdByUserAccount(@Param("userAccount") String userAccount);

    User getUserByAccount(@Param("userAccount") String userAccount);

    Integer deleteOne(@Param("id") Integer id);

    List<UserDTO> getUserByTagNameList(@Param("tagNameList") List<String> tagNameList);

    List<UserDTO> getAllUsers();

    List<UserDTO> getUserByAnyTagName(@Param("tagNameList") List<String> tagNameList);

    List<UserDTO> getuserByAnyTagname_josn(@Param("tagNameList")List<String> tagNameList);

    Integer updateOneUser(@Param("data") UserDTO data);

    /**
     * 批量插入数据库
     * @param users
     */
    Long batchInsert(@Param("users") List<User> users);
}
