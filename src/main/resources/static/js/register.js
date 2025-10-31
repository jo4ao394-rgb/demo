var apiurl={
    registerurl:"/register",
    registersuccess:"/"
}

$("#sub").click(function () {
    var username = $("#username").val();
    var password = $("#password").val();
    var phone = $("#phone").val();
    var email = $("#email").val();
    var gender = $("input[name='gender']:checked").val();

    // 簡單驗證：檢查哪些欄位為空
    var errors = [];
    if (!username) errors.push("用戶名稱");
    if (!password) errors.push("密碼");
    if (!phone) errors.push("手機號碼");
    if (!email) errors.push("電子郵件");
    if (!gender) errors.push("性別");

    // 如果有空欄位，顯示錯誤訊息
    if (errors.length > 0) {
        $("#errorMsg").text(errors.join("、") + " 不能為空").css("color","red").show();
        return;
    }

    // 隱藏錯誤訊息
    $("#errorMsg").hide();

    // 發送註冊請求
    $.post({
        url:apiurl.registerurl,
        contentType:"application/json;charset=UTF-8",
        data:JSON.stringify({"username":username,"password":password,"gender":gender,"phone":phone,"email":email}),
        //xhrFields: { withCredentials: true },
        success:function(res) {
            // 註冊成功，顯示訊息並3秒後跳轉
            $("#errorMsg").text("註冊成功！3秒後跳轉到首頁重新登入").css("color","green").show();
            setTimeout(function() {
                window.location.href = apiurl.registersuccess;
            }, 3000);
        },
        
        error: function(xhr) {
            // 顯示後端錯誤訊息（如：帳號重複）
            $("#errorMsg").text(xhr.responseText).css("color","red").show();
        }
    });
});

