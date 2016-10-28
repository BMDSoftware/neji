<%@ page import="pt.ua.tm.neji.web.services.Service" %>
<%@ page import="pt.ua.tm.neji.context.Context" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collection" %>
<%@ page import="pt.ua.tm.neji.dictionary.Dictionary" %>
<%@ page import="pt.ua.tm.neji.ml.MLModel" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserLevel" %>
<%@ page import="pt.ua.tm.neji.core.parser.ParserSupport" %>
<%@ page import="java.util.List" %>
<%@ page import="pt.ua.tm.neji.web.Server" %>
<html>
<% Iterable<Service> services = Server.getInstance().getActiveServices(); %>

<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/libs/jquery.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/edit.js"></script>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/bootstrap.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/bootstrap-responsive.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/becas.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/neji.css">
    <style type="text/css">
        img {
            margin: 10px;
        }
        .wrap {
            width:80%;
            margin: 0 auto;
        }
        .boxNew {
            margin: 10px;
            padding: 20px;
            width:80%;
            border: 1px dashed gray;
            border-radius:25px;
            height: 60px;
        }
        .box {
            margin: 10px;
            padding: 20px;
            width: 80%;
            height: 120px;
            border: 1px solid gray;
            border-radius:25px;
        }
        .left_col {
            float:left;
            width:90%;
        }
        .right_col {
            margin: 0 auto;
            float:left;
            vertical-align: middle;
            align-content: center;
            width:10%;
        }
    </style>
    <title>neji server</title>
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
            <li class="active"><a href="#">Services</a></li>
            <li><a href="/admin">Admin</a></li>
        </ul>
    </div>
</div>
<div class="wrap">
    <div>
        <h3>server services:</h3>
    </div>
    <div class="boxNew">
        <form action="/services/" method="POST" enctype="multipart/form-data">
            <div class="left_col">
                <h4>create a new service</h4>
                Name: <input id="serviceName" name="serviceName" type="text" />
            </div>
            <div class="right_col">
                <br>
                <br>
                <input type="image" name=press src="${pageContext.request.contextPath}/assets/img/add.png" value="Add" />
            </div>
        </form>
    </div>

    <% for(Service s : services) { %>
        <div class="box">
            <div class="left_col">
                <h4><b><%= s.getId() %></b></h4>
                <p>Using <%= s.getDictionaryRefs().size() %> dictionaries and <%= s.getModelRefs().size() %> models.</p>
                <p>
                    <% if(!s.includeAnnotationsWithoutIDs()) { %>
                    not
                    <% } %>
                    including annotations without IDs.
                </p>
                <p>
                    Parsing level: <%= s.getParserLevel() %>
                </p>
            </div>
            <div class="right_col">
                <a href="/services/<%= s.getId() %>"><img src="${pageContext.request.contextPath}/assets/img/annotate.png"/></a><br/>
                <a href="/services/<%= s.getId() %>/edit"><img src="${pageContext.request.contextPath}/assets/img/edit.png"/></a><br/>
                <a href="/services/<%= s.getId() %>/delete"><img src="${pageContext.request.contextPath}/assets/img/delete.png"/></a><br/>
            </div>
        </div>
    <% } %>
</div>

</body>
</html>
