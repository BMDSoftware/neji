<%@ page import="pt.ua.tm.neji.web.services.Service" %>
<%@ page import="pt.ua.tm.neji.context.Context" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collection" %>
<%@ page import="pt.ua.tm.neji.dictionary.Dictionary" %>
<%@ page import="pt.ua.tm.neji.ml.MLModel" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserLevel" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserSupport" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserTool" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserLanguage" %>
<%@ page import="java.util.List" %>
<%@ page import="pt.ua.tm.neji.web.Server" %>
<html>
<% Context context = Server.getInstance().getContext(); %>
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/libs/jquery.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/edit.js"></script>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/becas.css" />
    <style type="text/css">
        .wrap {
            width:90%;
            margin:0 auto;
        }
        .full_col {
            margin-top: 30px;
            margin-bottom: 30px;
            list-style:none;
            clear:both;
        }
        .left_col {
            float:left;
            width:40%;
        }
        .right_col {
            float:right;
            width:40%;
        }
        .box {
            margin: 10px;
            padding: 20px;
            width: 85%;
            height: 20px;
            border: 1px solid gray;
            border-radius:10px;
        }
        .box_left {
            float:left;
            width:80%;
        }
        .box_right {
            margin: 0 auto;
            float:left;
            text-align: right;
            vertical-align: middle;
            align-content: center;
            width:20%;
        }
    </style>
    <title>neji server</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/neji.css">
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner navbar-inner-neji">
        <!--<div class="brand brand-neji">-->
        <!--<a href="#">-->
        <!--<div class="lettering-small pull-left">neji</div>-->
        <!--<i class="icon-pencil pencil-small pull-left"></i>-->
        <!--</a>-->
        <!--</div>-->
        <ul class="nav">
            <li><a href="/">Home</a></li>
            <li><a href="/services">Services</a></li>
            <li class="active"><a href="#">Admin</a></li>
        </ul>
    </div>
</div>
<div class="wrap">
    <h3> Server configuration: </h3>
    <div class="full_col">
        <div class="left_col">
            <p>Dictionaries:</p>
            <% for(Map.Entry<String, Dictionary> e : context.getDictionaryPairs()) { %>
            <div class="box">
                <div class="box_left">
                    <%= e.getKey() %>
                </div>
                <div class="box_right">
                    <a href="/admin/delete/dictionary=<%= e.getKey() %>"><img src="${pageContext.request.contextPath}/assets/img/delete.png"/></a>
                </div>
            </div>
            <% } %>
        </div>
        <div class="left_col">
            <p>Models:</p>
            <% for(Map.Entry<String, MLModel> e : context.getModelPairs()) { %>
            <div class="box">
                <div class="box_left">
                    <%= e.getKey() %>
                </div>
                <div class="box_right">
                    <a href="/admin/edit/model=<%= e.getKey() %>"><img src="${pageContext.request.contextPath}/assets/img/edit.png"/></a>
                    <a href="/admin/delete/model=<%= e.getKey() %>"><img src="${pageContext.request.contextPath}/assets/img/delete.png"/></a>
                </div>
            </div>
            <% } %>
        </div>
</div>
</body>
</html>
