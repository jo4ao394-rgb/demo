//將用戶資料庫的值轉換成Java物件
package com.example.demo.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.demo.entity.User;
//RowMapper<User>將資料庫查詢出來的數據，轉換成是物件(這裡是將結果塞到User類裡面)
public class UserRowMapper implements RowMapper<User>{
    @Override //覆蓋
    //mapRow每一列轉換為Java物件 ResultSet代表結果集 i第幾列 throws SQLException判斷是否錯誤
    public User mapRow(ResultSet resultSet, int i) throws SQLException {

        User user=new User();

        //後面result.getxx()，代表從資料庫取此欄位的值()裡面放的是要取得的資料在資料庫中的名字
        //user.setId再放入到User類的id欄位中
        user.setUid(resultSet.getInt("Uid"));
        user.setUsername(resultSet.getString("Username"));
        user.setPassword(resultSet.getString("Password"));
        user.setSalt(resultSet.getString("Salt"));
        user.setGender(resultSet.getInt("Gender"));
        user.setPhone(resultSet.getString("Phone"));
        user.setEmail(resultSet.getString("Email"));
        user.setAvatar(resultSet.getString("Avatar"));
        user.setIsDelete(resultSet.getInt("Is_delete"));

        user.setCreatedBy(resultSet.getString("Created_by"));
        //Timestamp時間轉LocalDateTime(他不能直接轉)
        user.setCreatedTime(resultSet.getTimestamp("Created_time").toLocalDateTime());
        user.setUpdatedBy(resultSet.getString("Updated_by"));
        user.setUpdatedTime(resultSet.getTimestamp("Updated_time").toLocalDateTime());
        return user;
    }
}
