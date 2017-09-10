package utilities;

import models.Article;

public class BuiltInArticles {

    public static Article GetNotice()
    {
        Article notice = Article.find.where().eq("name","NOTICE").findUnique();

        if(notice == null){
            // We didn't create notice page, create it on demand
            notice = new Article("NOTICE", "NOTICE", "<Notice content>", Article.ArticleType.BUILTIN);
        }
        return notice;
    }

    public static Article GetTerms()
    {
        Article terms = Article.find.where().eq("name","TERMS").findUnique();

        if(terms == null){
            // We didn't create terms page, create it on demand
            terms = new Article("TERMS", "TERMS", "<Terms content>", Article.ArticleType.BUILTIN);
        }
        return terms;
    }

    public static Article GetOrganizerTemplate()
    {
        Article template = Article.find.where().eq("name", "NEW_TICKET_NOTIFICATION_TO_ORGANIZER").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(template == null)
        {
            template = new Article("NEW_TICKET_NOTIFICATION_TO_ORGANIZER", "[微票网] - {activity_name} - 用户订购", "<div style=\"background-color: #f9f9f9; color:#666;\">\t<div style=\"min-width: 300px;  max-width: 800px; margin:0 auto auto; padding:3%;\">\t\t<p><a href=\"http://weticket.com\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/851a6abf-b234-4318-930e-61a241f70ee9\" style=\"min-width: 50px;  max-width: 15%;  margin: 0px 80% 0px 0%;\"></a></p>\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350;\">\t\t\tHI, {user} 恭喜您的 <br>    \t\t\t<a href=\"{activity_link}\">             {activity}             </a>            {type} 类别票务卖出{quantity}张\t\t</p>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"background-color: #ededed;\t\tpadding: 5% 0; width: 100%;\">\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<div style=\"font-size: 20px;\"> 票务卖出详情</div>\n" +
                    "<div style=\"font-size: 20px;\"><br></div>\n" +
                    "\t\t\t\t<div>客户{buyer}购买</div>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<span style=\"width: 40%; display:inline-block;float:left;\">活动</span>\t\t\t\t<span style=\"width: 30%; display:inline-block;float:left;\">票类别</span>\t\t\t\t<span style=\"width: 20%; display:inline-block;float:left;\">数量</span>\t\t\t</div>\n" +
                    "\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<span style=\" width: 40%; display:inline-block;\">{activity}</span>\t\t\t\t<span style=\" width: 30%; display:inline-block;\">{type} </span>\t\t\t\t<span style=\"width: 20%; display:inline-block;\">{quantity}张</span>\t\t\t</div>\n" +
                    "\t\t\t<div style=\"margin-top: 5%;\t\t\tfont-weight: 200;\t\t\tclear:both;\t\t\tfont-size: 12px;\t\t\tdisplay:block;\t\t\tposition: relative;\t\t\ttext-align: center;  \t\t\twidth: 100%;\">\t\t\tThis order is subject to WeTicket <a style=\"\t\t\ttext-decoration: none;\" href=\"http://weticket.ca/about\">Terms of Service</a></div>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div>\t\t\t<br>\t\t\t已售出票务可在管理操作中的票务管理查看与检票 <br><br>\t          如果您有任何问题 欢迎致电:<a href=\"tel:7785583900\">778-558-3900</a><br><br>\t          感谢您选用微票购票。\t          <hr style=\"border: 0;\t\t\theight: 0;\t\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t</div>\n" +
                    "\t\t <div style=\" display:block; clear:both; text-align:center;\"> \t\t  <br>          <div>             <a href=\"http://weticket.ca/tickets\">              <img style=\"min-width: 300px;  max-width: 40%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/5f1790b1-0477-4176-8422-a529d72895c1\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 20px;\">管理账户</div>\n" +
                    "            <div>              <a href=\"http://weticket.ca/tickets\">登陆</a>               账户去管理您的票务.            </div>\n" +
                    "          </div>\n" +
                    "          <div class=\"clear\"></div>\n" +
                    "          <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "        </div>\n" +
                    "        <div style=\"width:45%; float: left;\"> \t\t  <br>          <div>             <a href=\"http://weticket.ca/\">              <img style=\"min-width: 50px;  max-width: 35%; padding-left:10%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/70be825c-614a-4295-9969-a5306a57af91\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 16px; margin-bottom:10px;\">创建自己的活动</div>\n" +
                    "              <div>每一个人都可以在微票上发起活动或者买票。快来试试吧</div>\n" +
                    "              <p>                <a href=\"http://weticket.ca/\">更多信息</a>              </p>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        <div style=\"width:45%; margin-left:10%;float: left;\"> \t\t  <br>          <div>             <a href=\"http://weticket.ca/\">              <img style=\"min-width: 50px;  max-width: 35%; padding-left:10%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/ec56efb1-ca58-4d85-aae4-a0834ebf0d95\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 16px; margin-bottom:10px;\">发现更多的活动</div>\n" +
                    "              <div>查找更多的活动，遇见更多的兴趣相同的朋友</div>\n" +
                    "              <p><a href=\"http://weticket.ca/\">马上登陆</a></p>\n" +
                    "          </div>\n" +
                    "                            </div>\n" +
                    "        <div style=\"clear: both;\"></div>\n" +
                    "        <br>        <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\tThis email was sent to you by      <a href=\"http://weticket.ca/\">weTicket</a> <br>      120B-13988 Maycrest Way Richmond<br>      <span class=\"glyphicon glyphicon-copyright-mark\" aria-hidden=\"true\"></span>       Copyright 2015 Spark Technology Inc.</div>\n" +
                    "\t</div>\n" +
                    "</div>",
                    Article.ArticleType.BUILTIN);
        }
        return template;
    }

    public static Article GetNewTicketTemplate()
    {
        Article template = Article.find.where().eq("name", "NEW_TICKET_NOTIFICATION").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(template == null)
        {
            template = new Article("NEW_TICKET_NOTIFICATION", "[微票网] - {activity_name} - 活动门票", "<div style=\"background-color: #f9f9f9; color:#666;\">\t<div style=\"min-width: 300px;  max-width: 800px; margin:0 auto auto; padding:3%;\">\t\t<p><a href=\"http://weticket.com\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/851a6abf-b234-4318-930e-61a241f70ee9\" style=\"min-width: 50px;  max-width: 15%;  margin: 0px 80% 0px 0%;\"></a></p>\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350;\">\t\t\tHI, {user} 恭喜您的 <br>    \t\t\t<a href=\"{activity_link}\">             {activity}             </a>\t\t\t票务已购买成功\t\t</p>\n" +
                    "\t\t<p>\t\t\t<!-- @@ the organizer -->\t\t\t由 &nbsp;<span style=\"color:#888;\t\t\ttext-decoration: underline;\">{organizer}</span>&nbsp;主办\t\t</p>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350;\">\t\t温馨留言，\t\t</p>\n" +
                    "\t\t<p>\t\t\t感谢您的订购，活动中见！ 如若您有任何问题欢迎联系主办方 {organizer}。<a href=\"mailto:{organizer_email}\">{organizer_email}</a>\t\t</p>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"background-color: #ededed;\t\tpadding: 5% 0; width: 100%;\">\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<div style=\"font-size: 20px;\"> 票务</div>\n" +
                    "\t\t\t\t<!-- @@ today's date -->\t\t\t\t<div>{date}</div>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<span style=\"width: 40%; display:inline-block;float:left;\">活动</span>\t\t\t\t<span style=\"width: 30%; display:inline-block;float:left;\">票类别</span>\t\t\t\t<span style=\"width: 20%; display:inline-block;float:left;\">数量</span>\t\t\t</div>\n" +
                    "\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t\t<span style=\" width: 40%; display:inline-block;\">{activity}</span>\t\t\t\t<span style=\" width: 30%; display:inline-block;\">{type} </span>\t\t\t\t<span style=\"width: 20%; display:inline-block;\">{quantity}张</span>\t\t\t</div>\n" +
                    "\t\t\t{tickets_begin}\t\t\t<div style=\"padding: 15px;\t\t\tborder-bottom: medium dotted #f9f9f9;\">\t\t\t<!-- @@ detailed ticket information -->\t\t\t\t<div style=\"text-align:center;\t\t\t\tmargin: 0 auto; padding: 3% 0;\">票号： {ticket_number}</div>\n" +
                    "\t\t\t\t<a href=\"{ticket_verify_link}\">\t\t\t\t<img src=\"{ticket_qr_code}\" style=\" display:block; clear:both; text-align:center;\t\t\t\tmargin: 0 auto; min-width: 260px;  max-width: 15%;\"> </a>\t\t\t</div>\n" +
                    "   \t\t\t{tickets_end}\t\t\t<div style=\"margin-top: 5%;\t\t\tfont-weight: 200;\t\t\tclear:both;\t\t\tfont-size: 12px;\t\t\tdisplay:block;\t\t\tposition: relative;\t\t\ttext-align: center;  \t\t\twidth: 100%;\">\t\t\tThis order is subject to WeTicket <a style=\"\t\t\ttext-decoration: none;\" href=\"http://weticket.ca/about\">Terms of Service</a></div>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div>\t\t\t<br>\t\t\t 票已自动加到您微票的票夹中，请到微票夹中查看。您可以长按二维码保存图片作为票据 <br><br>\t          如果您有任何问题 欢迎致电:<a href=\"tel:7785583900\">778-558-3900</a><br><br>\t          感谢您选用微票购票，活动中间。\t          <hr style=\"border: 0;\t\t\theight: 0;\t\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t</div>\n" +
                    "\t\t <div style=\" display:block; clear:both; text-align:center;\"> \t\t  <br>          <div>             <a href=\"http://weticket.ca/tickets\">              <img style=\"min-width: 300px;  max-width: 40%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/5f1790b1-0477-4176-8422-a529d72895c1\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 20px;\">管理账户</div>\n" +
                    "            <div>              <a href=\"http://weticket.ca/tickets\">登陆</a>               账户去管理您的票务.            </div>\n" +
                    "          </div>\n" +
                    "          <div class=\"clear\"></div>\n" +
                    "          <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "        </div>\n" +
                    "        <span style=\" width: 250px; display:inline-block;\">\t\t  <br>          <div>             <a href=\"http://weticket.ca/\">              <img style=\"min-width: 50px;  max-width: 35%; padding-left:10%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/70be825c-614a-4295-9969-a5306a57af91\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 16px; margin-bottom:10px;\">创建自己的活动</div>\n" +
                    "              <div>每一个人都可以在微票上发起活动或者买票。快来试试吧</div>\n" +
                    "              <p>                <a href=\"http://weticket.ca/\">更多信息</a>              </p>\n" +
                    "          </div>\n" +
                    "        </span>        <span style=\" width: 250px; display:inline-block; margin-left:10%\">\t\t  <br>          <div>             <a href=\"http://weticket.ca/\">              <img style=\"min-width: 50px;  max-width: 35%; padding-left:10%;\" src=\"http://weticketdev.elasticbeanstalk.com/image/ec56efb1-ca58-4d85-aae4-a0834ebf0d95\">            </a>          </div>\n" +
                    "          <div>             <div style=\"font-size: 16px; margin-bottom:10px;\">发现更多的活动</div>\n" +
                    "              <div>查找更多的活动，遇见更多的兴趣相同的朋友</div>\n" +
                    "              <p><a href=\"http://weticket.ca/\">马上登陆</a></p>\n" +
                    "          </div>\n" +
                    "                            </span>        <div style=\"clear: both;\"></div>\n" +
                    "        <br>        <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\tThis email was sent to you by      <a href=\"http://weticket.ca/\">weTicket</a> <br>      120B-13988 Maycrest Way Richmond<br>      <span class=\"glyphicon glyphicon-copyright-mark\" aria-hidden=\"true\"></span>       Copyright 2015 Spark Technology Inc.</div>\n" +
                    "\t</div>\n" +
                    "</div>",
                    Article.ArticleType.BUILTIN);
        }
        return template;
    }

    public static Article GetUserConfirmTemplate()
    {
        Article template = Article.find.where().eq("name","EMAIL_CONFIRM").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(template == null)
        {
            template = new Article("EMAIL_CONFIRM", "[微票网] - 用户验证", "<div style=\"background-color: rgb(249, 249, 249);\">\t<div style=\"min-width: 300px; margin: 0px auto auto; padding: 3%;\">\t\t<p style=\"color: rgb(102, 102, 102);\"><a href=\"http://weticket.com\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/558f391a-e439-4310-ba17-cd8cbb5906fd\" style=\"min-width: 50px;  max-width: 15%;  margin: 0px 80% 0px 0%;\"></a></p>\n" +
                    "\t\t<p style=\"color: rgb(248, 166, 58); font-size: 20px;\">\t\t\t亲爱的 {user},\t\t</p>\n" +
                    "\t\t<p><span style=\"color: rgb(102, 102, 102);\">\t\t\t您的微票网账户已经创建成功。</span><br><span style=\"color: rgb(102, 102, 102);\">\t\t\t请您点击以下连接验证邮箱：<a href=\"{link_url}\">马上验证</a></span></p>\n" +
                    "\t\t<hr style=\"color: rgb(102, 102, 102); border-width: 1px 0px; border-right-style: initial; border-left-style: initial; border-right-color: initial; border-left-color: initial; border-image-source: initial; border-image-slice: initial; border-image-width: initial; border-image-outset: initial; border-image-repeat: initial; height: 0px; border-top-style: solid; border-top-color: rgba(0, 0, 0, 0.0980392); border-bottom-style: solid; border-bottom-color: rgba(255, 255, 255, 0.298039);\">\n" +
                    "\t\t<p style=\"color: rgb(102, 102, 102); font-size: 20px;\">\t\t温馨提示，\t\t</p>\n" +
                    "\t\t<p style=\"color: rgb(102, 102, 102);\">\t\t\t如若以后您需要更改密码，可以在微票顶部的修改密码进行更改。\t\t</p>\n" +
                    "\t\t\t\t<div style=\"color: rgb(102, 102, 102);\">           <p>感谢您选用微票</p>\n" +
                    "          <div class=\"signiture\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/3708bf9c-6586-40fc-bbf4-15a807b745e1\" style=\" min-width: 60px; max-width: 15%;\"></div>\n" +
                    "                    <div>微票团队上</div>\n" +
                    "        </div>\n" +
                    "        <hr style=\"color: rgb(102, 102, 102); border-width: 1px 0px; border-right-style: initial; border-left-style: initial; border-right-color: initial; border-left-color: initial; border-image-source: initial; border-image-slice: initial; border-image-width: initial; border-image-outset: initial; border-image-repeat: initial; height: 0px; border-top-style: solid; border-top-color: rgba(0, 0, 0, 0.0980392); border-bottom-style: solid; border-bottom-color: rgba(255, 255, 255, 0.298039);\">\n" +
                    "\t\t<div style=\"color: rgb(102, 102, 102); margin-top: 5%; font-weight: 200; clear: both; font-size: 12px; display: block; position: relative; text-align: center; width: 100%;\">\t\t\t如果您没有试图验证您的微票账户，请无视这封邮件\t\t</div>\n" +
                    "\t\t<hr style=\"color: rgb(102, 102, 102); border-width: 1px 0px; border-right-style: initial; border-left-style: initial; border-right-color: initial; border-left-color: initial; border-image-source: initial; border-image-slice: initial; border-image-width: initial; border-image-outset: initial; border-image-repeat: initial; height: 0px; border-top-style: solid; border-top-color: rgba(0, 0, 0, 0.0980392); border-bottom-style: solid; border-bottom-color: rgba(255, 255, 255, 0.298039);\">\n" +
                    "\t\t<div style=\"color: rgb(102, 102, 102); margin-top: 5%; font-weight: 200; clear: both; font-size: 12px; display: block; position: relative; text-align: center; width: 100%;\">\t\tThis email was sent to you by      <a href=\"http://weticket.ca/\">weTicket</a> <br>      120B-13988 Maycrest Way Richmond<br>      <span class=\"glyphicon glyphicon-copyright-mark\" aria-hidden=\"true\"></span>       Copyright 2015 Spark Technology Inc.</div>\n" +
                    "\t</div>\n" +
                    "</div>"
                    , Article.ArticleType.BUILTIN);
        }
        return template;
    }

    public static Article GetAdminGeneratedUserConfirmTemplate()
    {
        Article template = Article.find.where().eq("name", "EMAIL_CONFIRM_BY_ADMIN").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(template == null)
        {
            template = new Article("EMAIL_CONFIRM_BY_ADMIN",
                    "[微票网] - 用户验证", "<div style=\"background-color: #f9f9f9; color:#666;\">\t<div style=\"min-width: 300px; max-width: 500; margin:0 auto auto; padding:3%;\">\t\t<p><a href=\"http://weticket.com\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/851a6abf-b234-4318-930e-61a241f70ee9\" style=\"min-width: 50px;  max-width: 15%;  margin: 0px 80% 0px 0%;\"></a></p>\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350; color: #f8a63a;\">\t\t\t亲爱的 {user},\t\t</p>\n" +
                    "\t\t<p>\t\t\t您的微票网账户已经创建，初始密码为：{pwdinfo}<br>\t\t\t请您点击以下连接验证邮箱并且更改密码：\t\t\t<span style=\"color:#7ebc42\">              <a href=\"{link_url}\">马上更改 </a>             </span>\t\t</p>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350;\">\t\t温馨提示，\t\t</p>\n" +
                    "\t\t<p>\t\t\t如若以后您需要更改密码，可以在微票顶部的修改密码进行更改。\t\t</p>\n" +
                    "\t\t\t\t<div>           <p>感谢您选用微票</p>\n" +
                    "          <div class=\"signiture\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/3708bf9c-6586-40fc-bbf4-15a807b745e1\" style=\" min-width: 60px; max-width: 15%;\"></div>\n" +
                    "                    <div>微票团队上</div>\n" +
                    "        </div>\n" +
                    "        <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\t\t如果您没有试图验证您的微票账户，请无视这封邮件\t\t</div>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\tThis email was sent to you by      <a href=\"http://weticket.ca/\">weTicket</a> <br>      120B-13988 Maycrest Way Richmond<br>      <span class=\"glyphicon glyphicon-copyright-mark\" aria-hidden=\"true\"></span>       Copyright 2015 Spark Technology Inc.</div>\n" +
                    "\t</div>\n" +
                    "</div>",
                    Article.ArticleType.BUILTIN);
        }
        return template;
    }

    public static Article GetResetPasswordTemplate()
    {
        Article template = Article.find.where().eq("name", "FORGET_PASSWORD").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(template == null)
        {
            template = new Article("FORGET_PASSWORD",
                    "[微票网] - 密码重置", "<div style=\"background-color: #f9f9f9; color:#666;\">\t<div style=\"min-width: 300px; max-width: 500; margin:0 auto auto; padding:3%;\">\t\t<p><a href=\"http://weticket.com\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/851a6abf-b234-4318-930e-61a241f70ee9\" style=\"min-width: 50px;  max-width: 15%;  margin: 0px 80% 0px 0%;\"></a></p>\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350; color: #f8a63a;\">\t\t\t亲爱的 {user},\t\t</p>\n" +
                    "\t\t<p><span style=\"line-height: 1.42857143;\">请您点击以下链接更改密码：\t\t\t</span><span style=\"line-height: 1.42857143; color: rgb(126, 188, 66);\">              <a href=\"{link_url}\">马上更改</a></span><br></p>\n" +
                    "<p><span style=\"color:#7ebc42\"><a href=\"{link_url}\"> </a>             </span>\t\t</p>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<p style=\"font-size: 20px;  font-weight: 350;\">\t\t温馨提示，\t\t</p>\n" +
                    "\t\t<p><span style=\"line-height: 1.42857143;\">该链接只有24小时的有效时间</span>。\t\t</p>\n" +
                    "\t\t\t\t<div>           <p>感谢您选用微票</p>\n" +
                    "          <div class=\"signiture\"><img src=\"http://weticketdev.elasticbeanstalk.com/image/3708bf9c-6586-40fc-bbf4-15a807b745e1\" style=\" min-width: 60px; max-width: 15%;\"></div>\n" +
                    "                    <div>微票团队上</div>\n" +
                    "        </div>\n" +
                    "        <hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\t\t如果您没有试图重置您的微票密码，请无视这封邮件\t\t</div>\n" +
                    "\t\t<hr style=\"border: 0;\t\theight: 0;\t\tborder-top: 1px solid rgba(0, 0, 0, 0.1);\t\tborder-bottom: 1px solid rgba(255, 255, 255, 0.3);\">\n" +
                    "\t\t<div style=\"margin-top: 5%;  font-weight: 200;  clear:both;  font-size: 12px;  display:block;  position: relative;  text-align: center;    width: 100%;\">\t\tThis email was sent to you by      <a href=\"http://weticket.ca/\">weTicket</a> <br>      120B-13988 Maycrest Way Richmond<br>      <span class=\"glyphicon glyphicon-copyright-mark\" aria-hidden=\"true\"></span>       Copyright 2015 Spark Technology Inc.</div>\n" +
                    "\t</div>\n" +
                    "</div>",
                    Article.ArticleType.BUILTIN);
        }
        return template;
    }

    public static Article GetCreateActivityTerm()
    {
        Article term = Article.find.where().eq("name", "CREATE_ACTIVITY_TERM").eq("type", Article.ArticleType.BUILTIN).findUnique();
        if(term == null){
            // We didn't create terms page, create it on demand
            term = new Article("CREATE_ACTIVITY_TERM", "创建活动条款", "<p class=\"p1\"><span class=\"s1\">对用户因使用本网站而产生的与第三方之间的纠纷，本网站不负任何责任。</span></p>\n" +
                    "<p class=\"p1\"><span class=\"s1\">本服务条款的最终解释权归Spark Technologies Inc. 公司所有</span></p>", Article.ArticleType.BUILTIN);
        }
        return term;
    }
}
