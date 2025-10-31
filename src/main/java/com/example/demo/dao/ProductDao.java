//建立商品介面
package com.example.demo.dao;

import java.util.List;

import com.example.demo.entity.Product;

public interface ProductDao {
    //找尋商品熱門排行
    List<Product> FindHostList();

    //找尋其餘商品
    List<Product> FindOtherList();

    //商品詳細數據
    Product ReadById(Integer id);
    
    //更新商品庫存
    Integer UpdateNum(Integer id, Integer num);
}
