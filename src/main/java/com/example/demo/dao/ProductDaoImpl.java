//定義商品介面
package com.example.demo.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Product;
import com.example.demo.mapper.ProductRowMapper;

@Component//將此類別註冊為Spring容器的Bean
//先創一個介面，這裡是給介面進行定義(商品資料)
public class ProductDaoImpl implements ProductDao {

    @Autowired //自動注入 NamedParameterJdbcTemplate可進行數據庫操作，執行SQL語句
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override //覆寫
    //商品熱門排行
    public List<Product> FindHostList() {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表中選出上架中的商品，並依照優先順序排序(降冪)，取前五筆
        String sql="SELECT * FROM Productdata WHERE Status=1 ORDER BY Priority DESC LIMIT 5";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //將選出的商品資料放入空的HashMap中(由於沒有像:id這種要填，所以會是空的)
        Map<String,Object> map= new HashMap<>();
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<Product> list =namedParameterJdbcTemplate.query(sql,map,new ProductRowMapper());

        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    //其餘商品排行
    public List<Product> FindOtherList() {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表中選出上架中的商品，並依照優先順序排序(降冪)，取前五筆
        String sql="SELECT * FROM Productdata WHERE Status=1 ORDER BY Priority DESC LIMIT 5 OFFSET 5";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //將選出的商品資料放入空的HashMap中(由於沒有像:id這種要填，所以會是空的)
        Map<String,Object> map= new HashMap<>();
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<Product> list =namedParameterJdbcTemplate.query(sql,map,new ProductRowMapper());

        if(list.size()>0){
            return list;
        }else {
            //如果是空集合[]
            return null;
        }
    }

    @Override //覆寫
    //商品詳細數據 
    //根據商品id查詢數據(從前端進行查詢後，至後端資料庫查找資料，然後再轉換成前端能判斷的物件回傳該id所有資料)
    public Product ReadById(Integer id) {
        //創建一個字串變數sql，裡面放SQL語句
        //在資料表中選出對應id的商品資料(前面Id為資料庫內欄位名稱，後面為自己自訂的名稱)
        String sql="SELECT * FROM Productdata WHERE Id=:id";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //將選出的商品id資料放入空的HashMap中
        Map<String,Object> map= new HashMap<>();
        //前面的id是上述sql語句後面那個名稱，後面是實際JAVA設定的名稱
        map.put("id",id);
        //SELECT，要用 query() 或 queryForObject()
        //執行SQL，讀取sql命令，從map找到對應的值並轉呈JAVA物件
        List<Product> list =namedParameterJdbcTemplate.query(sql,map,new ProductRowMapper());

        if(list.size()>0){
            return list.get(0); //回傳第一筆資料
        }else {
            //如果是空集合[]
            return null;
        }
    }
    
    @Override //覆寫
    //更新商品庫存
    public Integer UpdateNum(Integer id, Integer num) {
        //創建一個字串變數sql，裡面放SQL語句
        //更新指定商品的庫存數量
        String sql = "UPDATE Productdata SET Num = :num WHERE Id = :id";
        //Map類似python字典 <String, Object>指定key和value的型別 Object可以放任何型別
        //HashMap是Map的實現類(先建立空值放資料)
        //將資料表後面的值名稱填上實際對應後端的值
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("num", num);
        
        //INSERT/ UPDATE / DELETE，使用 update()，把上面所宣告的sql和map這兩個變數依照順序傳進去
        //執行SQL，讀取sql命令，從map找到對應的值
        int rows = namedParameterJdbcTemplate.update(sql, map); //回傳1代表更新一筆資料0代表沒有
        return rows;
    }
}
