//定義購物車介面
package com.example.demo.dao;

import java.time.LocalDateTime;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartVO;
import com.example.demo.mapper.CartRowMapper;
import com.example.demo.mapper.CartVORowMapper;

@Component
public class CartDaoImpl implements CartDao {

    @Autowired //自動注入 NamedParameterJdbcTemplate可進行數據庫操作，執行SQL語句
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override //覆寫
    //判斷購物車有無商品
    public List<Cart> FindByUidandPid(Integer uid, Integer pid) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql="SELECT * FROM Cartdata WHERE Uid=:uid AND Pid=:pid";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map= new HashMap<>();
        map.put("uid",uid);
        map.put("pid",pid); //填入前端輸入值
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<Cart> list =namedParameterJdbcTemplate.query(sql,map,new CartRowMapper());

        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    @Override //覆寫
    //新增購物車商品
    public Integer CreateCart(Cart cart) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql= "INSERT INTO Cartdata" +
                "(Uid, Pid, Num, Price, Created_by, Created_time, Updated_by, Updated_time)" +
                "VALUES " +
                "(:uid, :pid, :num, :price, :created_by, :created_time, :updated_by, :updated_time)";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map= new HashMap<>();
        map.put("uid",cart.getUid());
        map.put("pid",cart.getPid());
        map.put("num",cart.getNum());
        map.put("price",cart.getPrice());
        map.put("created_by",cart.getCreatedBy());
        map.put("created_time",cart.getCreatedTime());
        map.put("updated_by",cart.getUpdatedBy());
        map.put("updated_time",cart.getUpdatedTime());

        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        int rows=namedParameterJdbcTemplate.update(sql,map); //回傳1代表更新一筆資料0代表沒有
        return rows;
    }

    @Override //覆寫
    //更新購物車商品數量
    public Integer UpdateNumByCid(Integer cid, Integer num, String updated_by, LocalDateTime updated_time) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql="UPDATE Cartdata SET Num=:num, Updated_by=:updated_by, " +
                "Updated_time=:updated_time where Cid=:cid";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map= new HashMap<>();
        map.put("num",num);
        map.put("cid",cid);
        map.put("updated_by",updated_by);
        map.put("updated_time",updated_time);
        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        int rows=namedParameterJdbcTemplate.update(sql,map); //更新的資料筆數
        return rows;

    }

    @Override //覆寫
    //根據uid查詢購物車商品
    public List<CartVO> FindByUid(Integer uid) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        //按照時間排序，最新加入的商品在最前面
        String sql="SELECT cid, uid, pid, Cartdata.num, Cartdata.price," +
                "title, Productdata.price AS realPrice FROM " +
                "Cartdata LEFT JOIN Productdata ON " +
                "Cartdata.pid=Productdata.id " +
                "WHERE Uid=:uid ORDER BY Cartdata.Created_time desc";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map= new HashMap<>();
        map.put("uid",uid);
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        //從購物車表Cartdata取出某使用者 (uid) 的所有購物車項目，同時把目前價格從Productdata一起查出來
        List<CartVO> list =namedParameterJdbcTemplate.query(sql,map,new CartVORowMapper());

        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    @Override //覆寫
    //根據cid查詢購物車商品
    public Cart FindByCid(Integer cid) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql="SELECT * FROM Cartdata WHERE Cid=:cid";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map= new HashMap<>();
        map.put("cid",cid);
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<Cart> list =namedParameterJdbcTemplate.query(sql,map,new CartRowMapper());

        if(list.size()>0){
            return list.get(0); //回傳第一筆資料
        }else {
            //如果是空集合[]
            return null;
        }
    }
    
    @Override //覆寫
    //根據cid刪除購物車商品
    public Integer DeleteByCid(Integer cid) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql = "DELETE FROM Cartdata WHERE Cid=:cid";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map = new HashMap<>();
        map.put("cid", cid);
        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        int rows = namedParameterJdbcTemplate.update(sql, map);
        return rows;
    }

    @Override //覆寫
    //根據uid刪除購物車商品
    public Integer DeleteByUid(Integer uid) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表新增資料，前面為欄位，後面為值
        String sql = "DELETE FROM Cartdata WHERE Uid=:uid";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String,Object> map = new HashMap<>();
        map.put("uid", uid);
        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        int rows = namedParameterJdbcTemplate.update(sql, map);
        return rows;
    }
}
