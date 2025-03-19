package com.qiang.once.importUserInfo;

/**
 * 最简单的读
 * 1. 创建excel对应的实体对象
 * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，
 * 3. 直接读即可
 */

public class UserInfoRead {
    /*public static void main(String[] args) {
        String fileName = "D:\\c\\study\\java\\code\\伙伴匹配项目\\伙伴匹配系统后端代码\\partner_backend\\src\\main\\resources\\prodExcel.xlsx";
//        ImportBYDoRead(fileName);
        List<UserInfo> list = EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        Map<String, List<UserInfo>> listMap = list.stream()
                .filter(userInfo -> StrUtil.isNotBlank(userInfo.getUsername()))
                .collect(Collectors.groupingBy(UserInfo::getUsername));
        System.out.println("元数据个数："+list.size());
        System.out.println("去重清洗后："+listMap.entrySet().size());

    }


    static public void ImportBYDoRead(String fileName){
        EasyExcel.read(fileName, UserInfo.class, new UserInfoListener()).sheet().doRead();//直接读取出来
    }*/


}
