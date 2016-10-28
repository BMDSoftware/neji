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
<html>
<% Map<String, Object> map = (Map<String, Object>) pageContext.findAttribute("it"); %>
<% Context context = (Context) map.get("context"); %>
<% Service service = (Service) map.get("service"); %>

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
        .left_col {
            float:left;
            width:40%;
        }
        .right_col {
            float:right;
            width:40%;
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
            <li class="active"><a href="/services">Services</a></li>
            <li><a href="/admin">Admin</a></li>
        </ul>
    </div>
</div>
<form action="/services/<%= service.getId() %>/edit/" method="POST" enctype="multipart/form-data">
    <div class="wrap">
        <h3> <%=service.getId()%>'s service configuration: </h3>
        <div class="full_col">
            <div class="left_col">
                Name of the service: <input id="serviceName" name="serviceName" type="text" placeholder="<%=service.getId()%>" />
            </div>
            <div class="right_col">
                <label>
                    <% if(service.includeAnnotationsWithoutIDs()) { %>
                    <input type="checkbox" id="noIds" name="noIds" checked />
                    <% } else { %>
                    <input type="checkbox" id="noIds" name="noIds" />
                    <% } %>
                    Include annotations without IDs
                </label>
                <br/>
                Parser level:
                <select required id="parserLevel" title="Specify the parser level">
                    <option value="">Specify the parser level</option>
                    <% List<ParserLevel> supportedLevels = ParserSupport.getEqualOrLowerSupportedLevels(
                            context.getConfiguration().getParserTool(),
                            context.getConfiguration().getParserLanguage(),
                            context.getConfiguration().getParserLevel()); %>
                    <% ParserLevel selectedLevel = service.getParserLevel(); %>
                    <% supportedLevels.remove(selectedLevel); %>

                    <option name="parserLevel" value="<%= selectedLevel.name() %>" selected><%= selectedLevel.name() %></option>
                    <% for (ParserLevel l : supportedLevels){ %>
                        <option name="parserLevel" value="<%= l.name() %>"><%= l.name() %></option>
                    <% } %>
                </select>
                <input id="parserLevelSelected" name="parserLevel" type="hidden" value=""/>
                <br/>
                <br/>
                <br/>
                <br/>
            </div>
        </div>
        <div class="full_col">
            <div class="left_col">
                <p>Choose what dictionaries to use for concept annotation:</p>
                <input type="button" id="checkAll1" value="All" />
                <input type="button" id="checkNone1" value="None" />
                <% Collection<String> dictRefs = service.getDictionaryRefs(); %>
                <% for(Map.Entry<String, Dictionary> e : context.getDictionaryPairs()) { %>
                <label>
                    <% if(dictRefs.contains(e.getKey())) { %>
                    <input type="checkbox"
                           id="<%= e.getKey() %>"
                           name="dict"
                           value="<%= e.getKey() %>"
                           checked />
                    <% } else { %>
                    <input type="checkbox"
                           id="<%= e.getKey() %>"
                           name="dict"
                           value="<%= e.getKey() %>" />
                    <% } %>
                    <%= e.getKey() %>
                </label>
                <br/>
                <% } %>
                <br/>
                <br/>
                <p>Add a new dictionary:</p>
                <input id="dictFile" name="dictFile" type="file" multiple value=""/>
            </div>
            <div class="right_col">
                <p>Choose what models to use for concept annotation:</p>
                <input type="button" id="checkAll2" value="All" />
                <input type="button" id="checkNone2" value="None" />
                <% Collection<String> modelRefs = service.getModelRefs(); %>
                <% for(Map.Entry<String, MLModel> e : context.getModelPairs()) { %>
                <label>
                    <% if(modelRefs.contains(e.getKey())) { %>
                    <input type="checkbox"
                           id="<%= e.getKey() %>"
                           name="model"
                           value="<%= e.getKey() %>"
                           checked />
                    <% } else { %>
                    <input type="checkbox"
                           id="<%= e.getKey() %>"
                           name="model"
                           value="<%= e.getKey() %>" />
                    <% } %>
                    <%= e.getKey() %>
                </label>
                <br/>
                <% } %>
                <br/>
                <br/>
                <p>Add a new model:</p>
                <input id="modelFile" name="modelFile" type="file" multiple value=""/>
            </div>
        </div>

        <div class="full_col">
            <br/>
            <br/>
            <input type="submit" name=press value="Done" />
        </div>
    </div>

</form>
</body>
</html>
