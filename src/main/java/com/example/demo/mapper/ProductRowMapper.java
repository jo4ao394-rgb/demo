//將商品資料庫的值轉換成Java物件
package com.example.demo.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.demo.entity.Product;
//RowMapper<Product>將資料庫查詢出來的數據，轉換成是物件(這裡是將結果塞到Product類裡面)
public class ProductRowMapper implements RowMapper<Product>{
    @Override //覆蓋
    //mapRow每一列轉換為Java物件 ResultSet代表結果集 i第幾列 throws SQLException判斷是否錯誤
    public Product mapRow(ResultSet resultSet, int i) throws SQLException {

        Product product=new Product();

        //後面result.getxx()，代表從資料庫取此欄位的值()裡面放的是要取得的資料在資料庫中的名字
        //user.setId再放入到User類的id欄位中
        product.setId(resultSet.getInt("Id"));
        product.setCategoryid(resultSet.getInt("Categoryid"));
        product.setType(resultSet.getString("Type"));
        product.setTitle(resultSet.getString("Title"));
        product.setPrice(resultSet.getInt("Price"));
        product.setNum(resultSet.getInt("Num"));
        
        // 讀取 BLOB 圖片
        byte[] imageBytes = resultSet.getBytes("Imagepath");
        product.setImagepath(imageBytes);
        
        product.setStatus(resultSet.getInt("Status"));
        product.setPriority(resultSet.getInt("Priority"));

        product.setCreatedBy(resultSet.getString("Created_by"));
        //Timestamp時間轉LocalDateTime(他不能直接轉)
        product.setCreatedTime(resultSet.getTimestamp("Created_time").toLocalDateTime());
        product.setUpdatedBy(resultSet.getString("Updated_by"));
        product.setUpdatedTime(resultSet.getTimestamp("Updated_time").toLocalDateTime());
        return product;
    }
}

