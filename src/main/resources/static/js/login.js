var apiurl={
    loginurl:"/userlogin",
    successloginurl:"/"
}

$("#enter").click(function () {
    var username = $("#username").val();
    var password = $("#password").val();


    $.post({
        url:apiurl.loginurl,
        contentType:"application/json;charset=UTF-8",
        data:JSON.stringify({"username":username,"password":password}),
        //xhrFields: { withCredentials: true }, // 重要：允許發送和接收 cookies
        success:function(res) {
            // 登入成功，顯示訊息並3秒後跳轉
            $("#errorMsg").text("登入成功！即將跳轉到首頁...").css("color","green").show();
            setTimeout(function() {
                window.location.href = apiurl.successloginurl;
            }, 1000);
        },

        error: function(xhr) {
            // 從後端錯誤回傳取出訊息（例如 IllegalArgumentException）
            console.log(xhr.responseText); // → IllegalArgumentException：登入失敗，帳號不存在
            $("#errorMsg").text(xhr.responseText).css("color","red").show();
        }
    });

});