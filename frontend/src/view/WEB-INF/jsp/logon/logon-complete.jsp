<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="webStorageBundleNames" type="java.util.List"--%>

<!DOCTYPE html>
<html>
    <head>
        <c:url var="startPageLink" value="/start.action"/>
        <%-- Proceed in 3 seconds anyway (even if JS code failed) --%>
        <meta http-equiv="refresh" content="3; URL='${startPageLink}'">

        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title><s:message code="logon.title"/></title>

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body>
        <form action="${startPageLink}" data-form="static" method="POST" data-initializer="logon-complete">
            <%-- To be populated by JS code --%>
            <input type="hidden" name="webStorageJson" value=""/>

            <script id="config:logon-complete" type="application/json">${emm:toJson(webStorageBundleNames)}</script>
        </form>
    </body>
</html>
