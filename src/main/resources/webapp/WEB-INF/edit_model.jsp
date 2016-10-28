<%@ page import="java.util.Map" %>
<%@ page import="pt.ua.tm.neji.ml.MLModel" %>
<html>
<% Map<String, Object> map = (Map<String, Object>) pageContext.findAttribute("it"); %>
<% MLModel model = (MLModel) map.get("model"); %>
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/libs/jquery.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/edit.js"></script>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/becas.css" />
    <style type="text/css">
        .wrap {
            width:80%;
            margin:0 auto;
        }
        .full_col {
            margin-top: 30px;
            margin-bottom: 30px;
            list-style:none;
            clear:both;
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
            <li class="active"><a href="/admin">Admin</a></li>
        </ul>
    </div>
</div>
<div class="wrap">
    <h3> Server configuration: </h3>
    <div class="full_col">
        <p>Normalization dictionaries:</p>
        <% for(String dict : model.getNormalizationDictionaryNames()) { %>
        <div class="box">
            <div class="box_left">
                <%= dict %>
            </div>
            <div class="box_right">
                <a href="/admin/edit/model=<%= model.getModelName() %>/delete/dictionary=<%= dict %>"><img src="${pageContext.request.contextPath}/assets/img/delete.png"/></a>
            </div>
        </div>
        <% } %>
        <br/>
        <br/>
        <p>Add a new dictionary:</p>
        <form action="/admin/edit/model=<%= model.getModelName() %>/" method="POST" enctype="multipart/form-data">
            <div class="box">
                <div class="box_left">
                    <input id="dictFile" name="dictFile" type="file" multiple value=""/>
                </div>
                <div class="box_right">
                    <input type="submit" name=press value="Upload" />
                </div>
            </div>
        </form>
    </div>
</div>
</body>
</html>
