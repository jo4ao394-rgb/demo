$(document).ready(function(){
  Checklogin();
  loading_cart_data();
});


function Checklogin() {
  $.ajax({
    url: "/users/session-username",
    dataType: "text",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      console.log(data);
      if(data != ""){
        if ( $("#check").length ) {
            $("#check").html('<a href="#" id="logout-btn">登出</a>');  //加上 id="logout-btn"
          }

          // 將使用者名稱顯示在購物車左側的 nav-link
          var userHtml = '歡迎，#{username}';
          userHtml = userHtml.replace(/#{username}/g, data);
          $("#user-info-link").html(userHtml);
      }else {
        alert("登入訊息已過期，請重新登入");
        window.location.href="/";
      }
    }
  });
}

function loading_cart_data() {
  $("#info").empty();
  $.ajax({
    url: "/carts/" ,
    dataType: "json",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      console.log(data);
      var totalAmount = 0;  //新增：計算總金額
      
      for(var i = 0 ;i < data.length ;i++ ){
        var producturl="product?=" + data[i].pid;
        var num=data[i].num;
        var title=data[i].title;
        var price=data[i].price;
        var pid=data[i].pid;  // 取得 pid
        var cid=data[i].cid;  // 取得 cid
        var itemTotal = num * price;  // 單項總價
        
        totalAmount += itemTotal;  //累加到總金額
        
        var html=
    '<div class="row">'+
      '<div class="col-3">'+
        '<div class="title">#{title}</div>'+
        '<img src="/products/image/' + pid + '" alt="商品圖片" style="width:80px; height:80px; object-fit:cover;">'+
      '</div>'+
      '<div class="col-2">商品價錢'+
        '<div class="price">#{price}</div>'+
      '</div>'+
     '<div class="col-3">數量'+
        '<div class="num" id="num">#{num}</div>'+
      '</div>'+
      '<div class="col-2">總和'+
        '<div class="totalprice">#{total}</div>'+
      '</div>'+
      '<div class="col-2">'+
        '<button class="btn btn-danger delete-btn" data-cid="#{cid}">刪除</button>'+
      '</div>'+
    '</div>';
        
        html = html.replace(/#{title}/g, title);
        html = html.replace(/#{price}/g, price);
        html = html.replace(/#{num}/g, num);
        html = html.replace(/#{total}/g, itemTotal);
        html = html.replace(/#{cid}/g, cid);
        
        $("#info").append(html);
      }
      
      //顯示總金額和結帳按鈕(改)
      var totalHtml = 
        '<div class="row mt-4">'+
          '<div class="col-12 text-end">'+
            '<h4>購物車總金額：<span style="color: #ff502f; font-weight: bold;">$' + totalAmount + '</span></h4>'+
            '<button class="btn btn-success btn-lg mt-3" id="checkout-btn" data-total="' + totalAmount + '">前往結帳</button>'+
          '</div>'+
        '</div>';
      $("#info").append(totalHtml);
    }
  });
}

// 刪除按鈕點擊事件
$(document).on("click", ".delete-btn", function(){
  var cid = $(this).data("cid");  // 取得 cid
  
  if(confirm("確定要刪除這個商品嗎？")){
    $.ajax({
      url: "/carts/" + cid + "/delete",
      type: "post",
      dataType: "text",
      success: function(res){
        alert(res);  // 顯示 "刪除成功"
        loading_cart_data();  // 重新載入購物車資料
      },
      error: function(xhr){
        alert("刪除失敗：" + xhr.responseText);
      }
    });
  }
});

// 登出按鈕點擊事件
$(document).on("click",'#logout-btn',function(e){
  e.preventDefault(); // 防止默認的連結行為
  
  $.ajax({
    url: "/sign_out",
    type: "get",
    xhrFields: { withCredentials: true },
    success:function(res) {
      console.log(res); // 顯示"登出成功"
      // 登出成功後跳轉到首頁
      window.location.href = "/";
    },
    error: function(xhr) {
      console.log("登出時發生錯誤");
      // 即使出錯也跳轉到首頁
      window.location.href = "/";
    }
  });
});

// 結帳按鈕點擊事件(改)
$(document).on("click", "#checkout-btn", function(){
  var totalAmount = $(this).data("total");
  
  if(totalAmount <= 0) {
    alert("購物車是空的，無法結帳！");
    return;
  }
  
  if(confirm("確定要結帳嗎？總金額：$" + totalAmount)) {
    // 顯示載入中訊息
    $("#checkout-btn").html('<span class="spinner-border spinner-border-sm" role="status"></span> 處理中...').prop('disabled', true);
    
    $.ajax({
      url: "/api/pay",
      type: "post",
      data: { totalAmount: totalAmount },
      dataType: "json",
      success: function(response) {
        console.log("結帳回應:", response);
        
        if(response.action && response.tradeInfo && response.tradeSha) {
          // 創建表單並提交到藍新金流
          var form = $('<form>', {
            'method': 'POST',
            'action': response.action,
            'target': '_blank'  // 在新視窗開啟
          });
          
          // 添加藍新金流需要的參數
          form.append($('<input>', {'type': 'hidden', 'name': 'MerchantID', 'value': response.merchantID}));
          form.append($('<input>', {'type': 'hidden', 'name': 'TradeInfo', 'value': response.tradeInfo}));
          form.append($('<input>', {'type': 'hidden', 'name': 'TradeSha', 'value': response.tradeSha}));
          form.append($('<input>', {'type': 'hidden', 'name': 'Version', 'value': response.version}));
          
          // 提交表單
          $('body').append(form);
          form.submit();
          
          // 提示用戶
          alert("即將跳轉到付款頁面，請完成付款流程");
          
        } else {
          alert("結帳失敗：回應格式錯誤");
        }
        
        // 恢復按鈕狀態
        $("#checkout-btn").html('前往結帳').prop('disabled', false);
      },
      error: function(xhr) {
        alert("結帳失敗：" + (xhr.responseText || "伺服器錯誤"));
        // 恢復按鈕狀態
        $("#checkout-btn").html('前往結帳').prop('disabled', false);
      }
    });
  }
});
