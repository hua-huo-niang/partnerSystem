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

    /**
     * 查询包含所有标签列表的所有用户
     * @param tagNameList  标签列表
     * @return List<UserDTO> 所有用户列表
     */
    List<UserDTO> getuserByAllTagname_josn(@Param("tagNameList")List<String> tagNameList);

    Integer updateOneUser(@Param("data") UserDTO data);

    /**
     * 批量插入数据库
     * @param users
     */
    Long batchInsert(@Param("users") List<User> users);

    /**
     * 获取用户表的所有数据
     * @return  Integer 查询出的总条数
     */
    Integer getAllUsersCount();

    /**
     * 根据起始和结束去获取分页的数据
     * @param start 起始下标
     * @param count 当前起始下标要移动的个数，也就是要获取多少个
     * @return  List<UserDTO> 用户数据
     */
    List<UserDTO> getUsersByPage(@Param("start") Integer start, @Param("count") Integer count);

    /**
     * 根据用户id查询tagName列表
     * @param userId 用户id
     * @return String 字符串形式的json数据
     */
    String getTagNameByUserId(@Param("id") Long userId);

    /**
     * 根据标签查找用户
     * 模糊查询，只要有一个标签包含就行
     * 并且是分页查询
     * @param tagNameList 用户标签列表
     * @param offset 分页的起始下标
     * @param size 当前页的大小
     * @return List<UserDTO> 符合条件的所有用户
     */
    List<UserDTO> getuserByAnyTagnamePage(@Param("tagNameList") List<String> tagNameList, @Param("offset") Integer offset, @Param("size") Integer size);


    /**
     * 根据标签查找用户
     * 模糊查询，只要有一个标签包含就行
     * 只查询前100条
     * @param tagNameList 标签列表
     * @param count 查询的条数
     * @return List<UserDTO> 符合条件的所有用户
     */
    List<UserDTO> getuserByAnyTagnamePageWithCount(@Param("tagNameList")List<String> tagNameList, @Param("count") Integer count);



}
