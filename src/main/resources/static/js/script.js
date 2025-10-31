//載入頁面時先在入熱門商品資訊（之後文章會寫）
//再來進入Checklogin();檢查是否登入來決定html的狀況，例如是登入按鈕還是登出按鈕
$(document).ready(function(){
  showHotList();
  showOtherList();
  Checklogin();
});

//熱門商品
function showHotList() {
  $("#hot-list").empty();
  $.ajax({
    url: "/products/list/hot",
    dataType: "json",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      hotlist=data;
      console.log(hotlist);
      for (var i = 0; i < hotlist.length; i++) {
        var html ='<div class="content col-md-2">'
            +'<img src="/products/image/#{id}" alt="#{title}" onerror="this.src=\'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22150%22 height=%22150%22%3E%3Crect fill=%22%23ddd%22 width=%22150%22 height=%22150%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 font-size=%2218%22 fill=%22%23999%22%3E無圖片%3C/text%3E%3C/svg%3E\'" style="width:100%; height:150px; object-fit:cover; margin-bottom:10px;">'//
            +'<div class="title">#{title}</div>'
            +'<div class="price">$#{price}</div><a class="d-flex justify-content-end" href="#{href}">查看商品</a>'
            +'</div>';
        html = html.replace(/#{id}/g, hotlist[i].id);//
        html = html.replace(/#{title}/g, hotlist[i].title);
        html = html.replace(/#{price}/g, hotlist[i].price);
        html = html.replace(/#{href}/g, "/product?id=" + hotlist[i].id);

        $("#hot-list").append(html);
      }
    },
    error: function() {
    }
  });
}

//其他商品
function showOtherList() {
  $("#other-list").empty();
  $.ajax({
    url: "/products/list/other",
    dataType: "json",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      otherlist=data;
      console.log(otherlist);
      for (var i = 0; i < otherlist.length; i++) {
        var html ='<div class="content col-md-2">'
            +'<img src="/products/image/#{id}" alt="#{title}" onerror="this.src=\'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22150%22 height=%22150%22%3E%3Crect fill=%22%23ddd%22 width=%22150%22 height=%22150%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 font-size=%2218%22 fill=%22%23999%22%3E無圖片%3C/text%3E%3C/svg%3E\'" style="width:100%; height:150px; object-fit:cover; margin-bottom:10px;">'
            +'<div class="title">#{title}</div>'
            +'<div class="price">$#{price}</div><a class="d-flex justify-content-end" href="#{href}">查看商品</a>'
            +'</div>';
        html = html.replace(/#{id}/g, otherlist[i].id);
        html = html.replace(/#{title}/g, otherlist[i].title);
        html = html.replace(/#{price}/g, otherlist[i].price);
        html = html.replace(/#{href}/g, "/product?id=" + otherlist[i].id);

        $("#other-list").append(html);
      }
    },
    error: function() {
    }
  });
}

var username=" ";

function Checklogin() {
  $.ajax({
    url: "/users/session-username",
    dataType: "text",
    //xhrFields: { withCredentials: true },
    success:function(data) {
      console.log(data);
      // 更新右側下拉內的登出連結（若存在）
      if ( $("#check").length ) {
        $("#check").html('<a href="#" id="logout-btn">登出</a>');
      }

      // 將使用者名稱顯示在購物車左側的 nav-link
      var userHtml = '歡迎，#{username}';
      userHtml = userHtml.replace(/#{username}/g, data);
      $("#user-info-link").html(userHtml);
      username = data;
    },error:function (){
      //alert("沒登入");
    }
  });
}

//在頁面Checklogin()時，會依據有無登入來設定js變數username的值，如果沒有設置到的話代表沒有登入，
//所以按下修改資料的超連結按鈕時，就會進行阻擋反饋
$(document).on("click",'#modified',function(){
  if(username == " "){
    alert("尚未登入");
  }else{
    window.location.href ="/users/@"+ username;
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