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
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
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
            <h3> Annotate: </h3>
            <div class="full_col">
                <div class="left_col">
                    <h5>Input:</h5>                       
                    <form>
                        <label>Input Format:</label>
                        <!--<input type="text" id="textbox_inputFormat" value="RAW"><br/><br/>-->
                        <form>                            
                            <input type="radio" name="inputFormat" value="RAW" checked>RAW<br/>
                            <input type="radio" name="inputFormat" value="BIOC">BIOC<br/>
                            <input type="radio" name="inputFormat" value="XML">XML<br/>
                            <input type="radio" name="inputFormat" value="BC2">BC2<br/>
                        </form>
                        <label>Output Format:</label>
                        <!--<input type="text" id="textbox_outputFormat" value="NEJI"><br/><br/>-->
                        <form>
                            <input type="radio" name="outputFormat" value="NEJI" checked>NEJI<br/>
                            <input type="radio" name="outputFormat" value="BIOC">BIOC<br/>
                            <input type="radio" name="outputFormat" value="A1">A1<br/>
                            <input type="radio" name="outputFormat" value="CONLL">CONLL<br/>
                            <input type="radio" name="outputFormat" value="JSON">JSON<br/>
                            <input type="radio" name="outputFormat" value="B64">B64<br/>
                            <input type="radio" name="outputFormat" value="XML">XML<br/>
                            <input type="radio" name="outputFormat" value="PIPE">PIPE<br/>
                            <input type="radio" name="outputFormat" value="PIPEXT">PIPEXT<br/>
                            <input type="radio" name="outputFormat" value="BC2">BC2<br/>
                        </form> 
                        <label>Text to annotate:</label>
                        <textarea rows="5" cols="50" id="textarea_text">Insert the text to annotate here ...</textarea><br/><br/>
                        <input type="button" id="button_annotate" onclick="button_annotate_onclick()" value="Annotate" />
                    </form>
                </div>
                <div class="left_col">
                    <p>Output:</p>
                    <textarea rows="5" cols="50" id="textarea_output">           
                    </textarea>
                </div>

                <SCRIPT LANGUAGE="JavaScript">

                    function button_annotate_onclick()
                    {
                        // Get parameters
                        var inputFormat = document.querySelector('input[name="inputFormat"]:checked').value;
                        var outputFormat = document.querySelector('input[name="outputFormat"]:checked').value;
                        var text = document.getElementById("textarea_text").value;
                        
                        // Handle text
                        text = text.split("\"").join("&quot;");
                        text = encodeURIComponent(text);

                        var req = new XMLHttpRequest();
                        req.overrideMimeType('text/json');

                        // Create the callback:
                        req.onreadystatechange = function () {
                            if (req.readyState !== 4)
                                return; // Not there yet
                            if (req.status !== 200) {
                                // Handle request failure here...
                                document.getElementById("textarea_output").value = "Invalid";
                                return;
                            }
                            // Request successful, read the response
                            var resp = req.responseText;
                            document.getElementById("textarea_output").value = resp;
                        }

                        var json = new Object();
                        json.inputFormat = inputFormat;
                        json.outputFormat = outputFormat;
                        json.text = text;
                        var jsonString = JSON.stringify(json);
                        
                        var url = "http://localhost:8010/test/testManage/annotate2/json=" + jsonString;
                        req.open("GET", url, true);
                        req.send();
                    }             

                </SCRIPT>
            </div>
    </body>
</html>
