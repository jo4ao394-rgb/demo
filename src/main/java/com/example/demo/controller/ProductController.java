//執行商品網頁操作(熱門商品、各個商品獨立頁面)
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;

@RestController //返回值為Java物件，也就是說返回給前端JSON格式的數據(網頁位置設定用)

@RequestMapping("/products") //設定此類別的路徑為/products
public class ProductController {

    @Autowired //自動注入 商品服務介面
    private ProductService productService;
    //
    @GetMapping("/list/hot") //Get請求(查詢資料)
    public List<Product> GetHostList(){
        return productService.GetHostList();
    }

    @GetMapping("/list/other") //Get請求(查詢資料)
    public List<Product> GetOtherList(){
        return productService.GetOtherList();
    }

    @GetMapping() //Get請求(查詢資料) (在網址後面?id=3)
    //@RequestBody用來接收前端傳過來的JSON數據並轉換成Java物件
    public Product ReadById(@RequestParam Integer id){
        return productService.ReadById(id);
    }
    
    @GetMapping("/image/{id}") //Get請求(查詢資料) (取得商品圖片)
    //@Path用來取得url路徑的值 PathVariable為直接將參數作為url一部份)
    //ResponseEntity用來返回HTTP響應，包括狀態碼、標頭和響應體
    public ResponseEntity<byte[]> GetProductImage(@PathVariable Integer id) {
        //根據商品ID查詢商品數據
        Product product = productService.ReadById(id);
        //如果商品存在且圖片數據不為空，則返回圖片數據
        //contentType(MediaType.IMAGE_JPEG)這個回應是 JPEG 圖片格式
        //把商品圖片的位元組資料（byte[]）放進 HTTP 回應中
        if (product != null && product.getImagepath() != null) {
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImagepath());
        }
        
        return ResponseEntity.notFound().build(); //如果商品不存在或圖片數據為空，返回404 Not Found響應
    }
}