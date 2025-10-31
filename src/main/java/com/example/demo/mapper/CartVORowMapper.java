//將購物車結帳資料庫的值轉換成Java物件
package com.example.demo.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.demo.entity.CartVO;
//RowMapper<Cart>將資料庫查詢出來的數據，轉換成是物件(這裡是將結果塞到Cart類裡面)
public class CartVORowMapper implements RowMapper<CartVO>{
    @Override //覆蓋
    //mapRow每一列轉換為Java物件 ResultSet代表結果集 i第幾列 throws SQLException判斷是否錯誤
    public CartVO mapRow(ResultSet resultSet, int i) throws SQLException {

        CartVO cartVO=new CartVO();

        //後面result.getxx()，代表從資料庫取此欄位的值()裡面放的是要取得的資料在資料庫中的名字
        //user.setId再放入到User類的id欄位中
        cartVO.setCid(resultSet.getInt("Cid"));
        cartVO.setUid(resultSet.getInt("Uid"));
        cartVO.setPid(resultSet.getInt("Pid"));
        cartVO.setNum(resultSet.getInt("Num"));
        cartVO.setPrice(resultSet.getInt("Price"));
        cartVO.setTitle(resultSet.getString("Title"));
        cartVO.setRealPrice(resultSet.getInt("RealPrice"));
        return cartVO;
    }
}