
//disable the "enter" key press, since it might cause log out in some cases
function stopRKey(evt) { 
  var evt = (evt) ? evt : ((event) ? event : null); 
  var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null); 
  if ((evt.keyCode == 13) && (node.type=="text"))  {return false;} 
} 

//print out the welcome message
if (window.console) {
  console.log("Welcome to WeTicket!");
}

//the onload functions
$(document).ready(function(){
	$("#valid-button-menu").click(function(){
		var theEmail = $("#login-email-menu").val();
		var thePassword = $("#login-password-menu").val();
		var user_data = { "user": theEmail, "password":Sha1.hash(thePassword) };

		$.rest("GET", "/api/v1/login", user_data,
			function(data){
				window.loginUser = data.email;
				window.loginuserRole = data.role;
				$(".logout-form-div").show();
				$("#login-form-div").hide();
				$("#user-name").html(data.email);
				if(data.role === "admin" || data.role === "org")
				{
					$(".admin-controller").show();
					$(".activity-controller").show();
					window.location.assign(window.location.href);
				}else
				{
					$(".admin-controller").hide();
					$(".activity-controller").hide();
					window.location.assign(window.location.href);
				}
			},
			function(data){
				alert("邮箱或密码错误！");
			}
		);
	});

	$("#register-button").click(function(){
		window.location.assign("/register");
	});

	// Very basic implementation of message because it is used by admin only
	var messagePolling = false;
	var processMessage = function(){
		if(window.loginUserRole === "admin" && !messagePolling){
			messagePolling = true;
			$.rest("GET", "/api/v1/getUnprocessedInfo", {}, function(r){
				messagePolling = false;
				var reqs = r.publishRequests;
				if(!isNaN(reqs) && reqs > 0){
					$("#unprocessed-info").html(reqs);
					$("#unprocessed-info").show();
				}
				else{
					$("#unprocessed-info").hide();
				}
			}, function(e){
				messagePolling = false;
			});
		}


	};
	setInterval(processMessage, 2000);
	processMessage();
	$(function () {
		$('[data-toggle="tooltip"]').tooltip()
	})

	$("#valid-button").click(function(){
		var theEmail = $("#login-email").val();
		var thePassword = $("#login-password").val();
		var user_data = { "user": theEmail, "password":Sha1.hash(thePassword) };

		$.rest("GET", "/api/v1/login", user_data,
			function(data){
				window.loginUser = data.email;

				window.loginuserRole = data.role;
				window.location.assign(window.location.href);
			},
			function(data){
				alert("邮箱或密码错误！");
			}
		);

	});
	
	
	$(".logout-button").click(function(){

		$.rest("GET", "/api/v1/logout", {},
			function(data){
				$(".logout-form-div").hide();
				$(".login-controller").hide();
				$(".admin-controller").hide();
				$(".activity-controller").hide();
				$("#login-form-div").show();
				$("#email").val('');
				$("#email").prop('disabled', false);
				//$("#password-div").show();
				$("#user-info").html("请在此处订票邮箱");

				//clear filled user information
				clearUserInfo();
			},
			function(data){
				alert("退出登陆失败");
			}
		);

	});
	
	//date time picker
    $('.form_datetime').datetimepicker({
        //language:  'fr',
        weekStart: 1,
        todayBtn:  1,
		autoclose: 1,
		todayHighlight: 1,
		startView: 2,
		forceParse: 0,
        showMeridian: 1
    });
	
	
});

$.rest = function(method, uri, data, success, error)
{
	$.ajax({

		type: method,

		url: uri,

		dataType: 'json',

		data: method.toUpperCase() === "GET" ? data : (jQuery.isPlainObject(data) ? JSON.stringify(data) : data),

		contentType: "application/json; charset=utf-8",

		success: function (data) {
			// we go here because everything is OK
			if (success) {
				success(data);
			}
		},

		error: function (data) {
			// something wrong in backend
			if (error) {
				error(data);
			}
		}
	});
}


//First, checks if it isn't implemented yet.
if (!String.prototype.format) {
  String.prototype.format = function() {
    var args = arguments;
    return this.replace(/{(\d+)}/g, function(match, number) { 
      return typeof args[number] != 'undefined'
        ? args[number]
        : match
      ;
    });
  };
}

function getPrimaryColor(url, callback){
	var colorThief = new ColorThief();
	var img = document.createElement('img');
	img.src = url;
	img.onload = function(){
		var color = colorThief.getColor(img);
		callback(color);
	}
}

