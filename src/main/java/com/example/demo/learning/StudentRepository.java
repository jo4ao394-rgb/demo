//建立資料庫操作介面
package com.example.demo.learning;

//import java.util.List;
//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

///import com.example.demo.controller.learning.Student;

//可在此自定義查詢條件
@Repository //能利用此註解操作資料庫
//interface代表介面(也就是這個介面繼承JpaRepository的規格，只要照規格走)
//JpaRepository<類, 類主鍵>
public interface  StudentRepository extends JpaRepository<Student, Integer> {
    //Optional< Student> findByName(String name);
}
