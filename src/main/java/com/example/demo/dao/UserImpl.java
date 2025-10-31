//定義用戶介面
package com.example.demo.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserRowMapper;
//import com.example.demo.dao.UserDao;

@Component //將此類別註冊為Spring容器的Bean
//先創一個介面，這裡是給介面進行定義(用戶資料)
public class UserImpl implements UserDao{

    @Autowired //自動注入 NamedParameterJdbcTemplate可進行數據庫操作，執行SQL語句
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override //覆寫
    //創建用戶(從前端進行註冊後的資料，儲存至後端資料庫)
    //String這裡是會回傳註冊成功
    public String CreateUser(User user) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql ="INSERT INTO Userdata" +
                "(Username,Password,Salt,Gender,Phone,Email,Avatar,Is_delete,Created_by,Created_time,Updated_by,Updated_time)" +
                " VALUES " +
                "(:userName,:userPassword,:Salt,:Gender,:Phone,:Email,:Avatar,:IsDelete,:created_by,:created_time,:updated_by,:updated_time)";
        
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String, Object> map =new HashMap<>();
        map.put("userName",user.getUsername());
        map.put("userPassword",user.getPassword());
        map.put("Salt",user.getSalt());
        map.put("Gender",user.getGender());
        map.put("Phone",user.getPhone());
        map.put("Email",user.getEmail());
        map.put("Avatar",user.getAvatar());
        map.put("IsDelete",user.getIsDelete());
        map.put("created_by",user.getCreatedBy());
        map.put("created_time",user.getCreatedTime());
        map.put("updated_by",user.getUpdatedBy());
        map.put("updated_time",user.getUpdatedTime());

        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        namedParameterJdbcTemplate.update(sql,map);

        return ("註冊成功");
    }

    @Override //覆寫
    //根據用戶名稱username查詢數據(從前端進行查詢後，至後端資料庫查找資料，然後再轉換成前端能判斷的物件回傳該用戶所有資料)
    public List<User> ReadByUsername(String username) {
        //創建一個字串變數sql，裡面放SQL語句
        String sql="SELECT * FROM Userdata WHERE Username=:userName";
        //將資料表後面的值名稱填上實際對應的值
        Map<String,Object>map= new HashMap<>();
        map.put("userName",username);
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<User> list =namedParameterJdbcTemplate.query(sql,map,new UserRowMapper());
        //回傳結果
        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    @Override //覆寫
    //根據用戶id查詢數據(從前端進行查詢後，至後端資料庫查找資料，然後再轉換成前端能判斷的物件回傳該id所有資料)
    public List<User> ReadByUid(Integer uid) {
        //創建一個字串變數sql，裡面放SQL語句
        String sql="SELECT * FROM Userdata WHERE Uid=:uid";
        //將資料表後面的值名稱填上實際對應的值
        Map<String,Object>map= new HashMap<>();
        map.put("uid",uid);
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<User> list =namedParameterJdbcTemplate.query(sql,map,new UserRowMapper());
        //回傳結果
        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    @Override //覆寫
    //根據用戶id修改數據
    //String這裡是會回傳修改成功
    public String UpdateByUid(Integer uid, User user) {
        //創建一個字串變數sql，裡面放SQL語句
        String sql ="UPDATE Userdata SET " +
                "Phone=:Phone,Email=:Email,Gender=:Gender ,Updated_by=:updated_by,Updated_time=:updated_time" +
                " WHERE Uid=:uid";
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String, Object> map =new HashMap<>();

        map.put("Phone",user.getPhone());
        map.put("Email",user.getEmail());
        map.put("Gender",user.getGender());
        map.put("updated_by",user.getUpdatedBy());
        map.put("updated_time",user.getUpdatedTime());
        map.put("uid",uid);
        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        namedParameterJdbcTemplate.update(sql,map);
        return "修改成功";
    }
}
