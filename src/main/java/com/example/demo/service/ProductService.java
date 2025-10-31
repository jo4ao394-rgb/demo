//建立商品服務介面
package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Product;

public interface ProductService {
    //輸入商品熱門排行
    List<Product> GetHostList();

    //輸入商品其餘排行
    List<Product> GetOtherList();

    //查詢商品詳細數據
    Product ReadById(Integer id);

    //更新購買後商品庫存
    Integer AddNum(Integer id, Integer num);

    //更新退款後商品庫存
    Integer SubNum(Integer id, Integer num);

}
