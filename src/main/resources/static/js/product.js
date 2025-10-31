var username=" ";
var producturl="/products" + window.location.search;
var pid="";

console.log("ProductURL:", producturl);


$(document).ready(function(){
  Checklogin();
  loading_product_data();
});


function Checklogin() {
  $.ajax({
    url: "/users/session-username",
    dataType: "text",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      console.log(data);
      if(data != ""){  
        username = data;//加這行：更新 username
        if ( $("#check").length ) {
            $("#check").html('<a href="/sign_out">登出</a>');
          }

          // 將使用者名稱顯示在購物車左側的 nav-link
          var userHtml = '歡迎，#{username}';
          userHtml = userHtml.replace(/#{username}/g, data);
          $("#user-info-link").html(userHtml);
      }else {
      }
    }
  });
}

function loading_product_data() {
  $.ajax({
    url: producturl,
    dataType: "json",
    success:function(data) {
      $("#itemname").text(data.title);
      $("#price").text("$" + data.price);
      $("#num").text("剩餘數量：" + data.num);

      // 先設定 pid
      pid = data.id;
      
      // 顯示圖片 (使用 BLOB 圖片 API)
      var imgHtml = '<img src="/products/image/' + pid + '" alt="商品圖片" style="width:100%; max-width:400px;">';
      $("#p_img").html(imgHtml);

      if(data.status == 0){
        alert("此商品已下架");
      }
    },
    error: function(xhr, status, error) {
      console.log("載入商品資料失敗:", error);
    }
  });
}

$(document).on("click",'#dec',function(){
  var num = parseInt($("input[name=productnum]").val());
  if(num==1){
    alert("已經是最小值")
  }else{
    $("input[name=productnum]").val(num-1);
  }

});

$(document).on("click",'#inc',function(){
  var num = parseInt($("input[name=productnum]").val());
  $("input[name=productnum]").val(num+1);
});


$(document).on("click",'#addtocart',function(){
  //先在發送請求前檢查登入狀態
  if(username == " "){
    alert("尚未登入");
    return;  // 直接返回，不發送請求
  }
  
  $("#errorMsg").text("");  // 清空錯誤訊息

  $.ajax({
    url:"/carts/addcart",
    data:{"pid":pid,"amount":$("input[name=productnum]").val()},
    //xhrFields: { withCredentials: true },
    type:"post",
    dataType:"text",
    success:function(res){
      if(res != ""){
        alert("加入成功");
      }
    },
    error: function(xhr) {
      // 顯示後端錯誤訊息（如：庫存不足）
      $("#errorMsg").text(xhr.responseText).css("color","red").show();
    }
  });
});