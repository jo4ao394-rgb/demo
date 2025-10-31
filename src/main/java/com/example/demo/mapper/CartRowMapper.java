//將購物車資料庫的值轉換成Java物件
package com.example.demo.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.demo.entity.Cart;
//RowMapper<Cart>將資料庫查詢出來的數據，轉換成是物件(這裡是將結果塞到Cart類裡面)
public class CartRowMapper implements RowMapper<Cart>{
    @Override //覆蓋
    //mapRow每一列轉換為Java物件 ResultSet代表結果集 i第幾列 throws SQLException判斷是否錯誤
    public Cart mapRow(ResultSet resultSet, int i) throws SQLException {

        Cart cart=new Cart();

        //後面result.getxx()，代表從資料庫取此欄位的值()裡面放的是要取得的資料在資料庫中的名字
        //user.setId再放入到User類的id欄位中
        cart.setCid(resultSet.getInt("Cid"));
        cart.setUid(resultSet.getInt("Uid"));
        cart.setPid(resultSet.getInt("Pid"));
        cart.setNum(resultSet.getInt("Num"));
        cart.setPrice(resultSet.getInt("Price"));

        cart.setCreatedBy(resultSet.getString("Created_by"));
        //Timestamp時間轉LocalDateTime(他不能直接轉)
        cart.setCreatedTime(resultSet.getTimestamp("Created_time").toLocalDateTime());
        cart.setUpdatedBy(resultSet.getString("Updated_by"));
        cart.setUpdatedTime(resultSet.getTimestamp("Updated_time").toLocalDateTime());
        return cart;
    }
}