<!DOCTYPE html>
<html lang="en" >
<head>
  <meta charset="UTF-8">
  <title>CodePen - SpingBoot商城－商品頁面</title>
  <link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.0.0/css/bootstrap.min.css'><link rel="stylesheet" href="/static/css/product.css">

</head>
<body>
<!-- partial:index.partial.html -->
<div class="navbar navbar-light bg-light navbar-expand-lg">
  <div class="container-fluid"><a class="navbar-brand" href="/">SpingBoot商城</a>
    <div class="nav-item">商品頁面</div>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarText" aria-controls="navbarText" aria-expanded="false" aria-label="Toggle navigation"> <span class="navbar-toggler-icon"></span></button>
    <div class="collapse navbar-collapse justify-content-end" id="navbarText">
      <ul class="navbar-nav">
        <li class="nav-item"><a class="nav-link" active="active" aria-current="page" href="/mycarts">購物車</a></li>
        <li class="nav-item dropdown"><a class="nav-link dropdown-toggle" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false" href="#">會員中心</a>
          <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
            <li>
              <button class="dropdown-item" id="modified">修改資料</button>
            </li>
            <li>
              <button class="dropdown-item" href="#">查看訂單</button>
            </li>
            <li>
              <button class="dropdown-item" id="check"><a href="/register">註冊</a> / <a href="/login">登入</a></button>
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</div>
<section id="product" class="d-flex align-items-center" style="min-height: 80vh;">
  <div class="container">
    <div class="row">
      <div class="col-12 text-center">
        <h1 class="title" id="itemname">商品名稱</h1>
      </div>
      <div class="col-lg-6 mx-auto text-center">
        <button class="btn" id="p_img">商品圖片</button>
        <div class="price" id="price">$價格</div>
        <div class="num" id="num">剩餘數量：</div>
        <div class="addcart">
          <button class="btn btn-primary" id="dec">-</button>
          <input name="productnum" type="text" value="1"/>
          <button class="btn btn-primary" id="inc">+ </button>
          <button class="btn btn-danger" id="addtocart">加入購物車</button>
        </div>
        <p id="errorMsg" style="color:red; margin-top:10px;"></p>
      </div>
      
      <!-- 分隔線 -->
      <div class="col-12 my-4">
        <hr style="border: 0; height: 2px; background: linear-gradient(to right, #ff502f, #fae3d9);">
      </div>
      
      <!-- 商品詳情 -->
      <div class="col-12 text-center">
        <h3>商品詳情</h3>
        <div class="product-detail">
          <p>商品詳情內容（之後新增）</p>
        </div>
      </div>
    </div>
  </div>
</section>
<!-- partial -->
  <script src='https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.0.0/js/bootstrap.min.js'></script>
<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js'></script><script  src="/static/js/product.js"></script>

</body>
</html>