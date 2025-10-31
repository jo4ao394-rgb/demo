//執行資料庫網頁操作(新增、查詢、更新、刪除)
package com.example.demo.learning;

import java.net.URI;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

//import com.example.demo.controller.learning.StudentRepository;

@RestController //返回值為Java物件，也就是說返回給前端JSON格式的數據(網頁位置設定用)
public class MyController {

    @Autowired //自動注入(將建立資料庫操作介面的物件注入進來)
    private  StudentRepository studentRepository;

    //新增資料
    @PostMapping("/students") //Post請求(新增資料)
    //ResponseEntity<類> 代表HTTP回應(HTTP要回傳物件) Void代表無返回值
    //@RequestBody POST來請求時，獲取前端放在request body中所傳遞的參數
    public ResponseEntity<Void> createStudent(@RequestBody Student student) {
        student.setId(null); //確保ID為null，讓資料庫自動生成
        studentRepository.save(student); //利用資料庫介面將儲存資料到資料庫
        
        //建立網址
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri() //取得目前請求的路徑(/products)
                .path("/{id}") //在目前路徑後面加上/id
                .build(Map.of("id", student.getId())); //getId取得真實值，將{id}替換成真實的值(Map.of創建不可變Map)

        return ResponseEntity.created(uri).build(); //返回201狀態碼，並在回應中包含新創建資源的URI
    }
    
    //查詢資料
    //單筆
    @GetMapping("/students/{id}") //Get請求(查詢資料)
    //ResponseEntity<操作類別> 代表HTTP回應(HTTP要回傳物件) 因為式查詢資料會有返回值，就會是Product類別的值
    //@PathVariable 獲取放在url中的參數(這裡是設id)
    public ResponseEntity<Student> getStudent(@PathVariable Integer id) {
        //Optional<類>如果有值返回值，沒有的話返回空值
        Optional<Student> studentOp = studentRepository.findById(id); //利用資料庫介面查詢id的資料
        return studentOp.isPresent() //判斷是否存在
                ? ResponseEntity.ok(studentOp.get()) //如果有值，返回200狀態碼，並在回應中包含查詢到的資源
                : ResponseEntity.notFound().build(); //如果沒有值，返回404狀態碼
    }

    //多筆(?idList=111,222,333)
    @GetMapping("/students/ids") //Get請求(查詢資料)
    //因為是多筆資料，所以返回值是List<Student>
    //@RequestParam 獲取放在url後面的參數(@RequestParam為?參數，@PathVariable為直接將參數作為url一部份)
    public ResponseEntity<List<Student>> getStudents(@RequestParam List<Integer> idList) {
        List<Student> students = studentRepository.findAllById(idList); //利用資料庫介面查詢多筆id的資料
        return ResponseEntity.ok(students); //返回200狀態碼，並在回應中包含查詢到的資源
    }

    //更新資料
    @PutMapping("/students/{id}") //Put請求(更新資料)
    //因為是更新id的資料，所以他要先獲取id，然後再用RequestBody獲取要更新的資料
    public ResponseEntity<Void> updateStudent(@PathVariable Integer id, @RequestBody Student request) {
        Optional<Student> studentOp = studentRepository.findById(id); //利用資料庫介面查詢id的資料
        //.isEmpty() 判斷是否為空值
        if (studentOp.isEmpty()) {
            return ResponseEntity.notFound().build(); //如果沒有值，返回404狀態碼
        }

        Student student = studentOp.get(); //獲取查詢到的資料
        if (request.getMark() == 0) {
            request.setMark(student.getMark());
        }else{
            student.setMark(request.getMark());
        }

        if (request.getName() == null) {
            request.setName(student.getName());
        }else{
            student.setName(request.getName());
        }
        //student.setMark(request.getMark());
        //student.setName(request.getName()); 

        //更新資料
        studentRepository.save(student); //利用資料庫介面將更新後的資料儲存到資料庫

        return ResponseEntity.noContent().build(); //返回204狀態碼(表示請求成功，但沒有內容返回)
    }

    //刪除資料
    @DeleteMapping("/students/{id}") //Delete請求(刪除資料)
    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
        studentRepository.deleteById(id); //利用資料庫介面刪除id的資料
        return ResponseEntity.noContent().build();
    }
}


/*
    @GetMapping("/products/{name}") //Get請求(查詢資料)
    //ResponseEntity<操作類別> 代表HTTP回應(HTTP要回傳物件) 因為式查詢資料會有返回值，就會是Product類別的值
    //@PathVariable 獲取放在url中的參數(這裡是設id)
    public ResponseEntity<Product> getProduct(@PathVariable String name) {
        //Optional<類>如果有值返回值，沒有的話返回空值
        Optional<Product> productOp = productRepository.findByName(name); //利用資料庫介面查詢id的資料
        return productOp.isPresent() //判斷是否存在
                ? ResponseEntity.ok(productOp.get()) //如果有值，返回200狀態碼，並在回應中包含查詢到的資源
                : ResponseEntity.notFound().build(); //如果沒有值，返回404狀態碼
    }
 */