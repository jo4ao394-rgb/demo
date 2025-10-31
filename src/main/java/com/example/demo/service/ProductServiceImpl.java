//定義商品服務介面
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.dao.ProductDao;
import com.example.demo.entity.Product;
//import com.example.demo.exception.ProductNotFoundException;

@Component //將此類別註冊為Spring容器的Bean
//先創一個介面，這裡是給介面進行定義
public class ProductServiceImpl implements ProductService{

    @Autowired //自動注入 ProductDao可執行商品介面
    private ProductDao productDao;

    @Override //覆寫
    //將找到的熱門商品列表回傳
    public List<Product> GetHostList() {
        List<Product> products = FindHostList();

        return products;
    }

    @Override //覆寫
    //將找到的其餘商品列表回傳
    public List<Product> GetOtherList() {
        List<Product> products = FindOtherList();

        return products;
    }

    //根據商品id查詢數據
    public Product ReadById(Integer id) {

        Product product=FindById(id); //回傳的是第一筆資料

        //判斷是否有查到商品數據，沒有的話就拋出異常
        if(product == null){
            //改用了IllegalArgumentException而不是ProductNotFoundExceptio
            throw new IllegalArgumentException("找不到商品數據");
        }
        return product;
    }

    @Override //覆寫
    //更新購買後商品庫存
    public Integer AddNum(Integer id, Integer num) {
        //查詢原始商品庫存
        Product product=FindById(id);
        //更新商品庫存(原本庫存-購買數量)
        product.setNum(product.getNum()-num);
        product.setUpdatedTime(LocalDateTime.now());
        //執行更新庫存
        Integer rows = productDao.UpdateNum(id, product.getNum());
        return rows;
    }

    @Override //覆寫
    //更新退款後商品庫存
    public Integer SubNum(Integer id, Integer num) {
        //查詢原始商品庫存
        Product product=FindById(id);
        //更新商品庫存(原本庫存+退款數量)
        product.setNum(product.getNum()+num);
        product.setUpdatedTime(LocalDateTime.now());
        //執行更新庫存
        Integer rows = productDao.UpdateNum(id, product.getNum());
        return rows;
    }


    private List<Product> FindHostList() {
        return productDao.FindHostList();
    }

    private List<Product> FindOtherList() {
        return productDao.FindOtherList();
    }

    private Product FindById(Integer id){
        return productDao.ReadById(id);
    } //兩個是私人的方法

}
