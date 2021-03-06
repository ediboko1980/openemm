<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>

<c:set var="agnNavigationKey" 		value="pluginmanager" 			scope="request" />
<c:set var="agnTitleKey" 			value="settings.pluginmanager" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.pluginmanager" 	scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.pluginmanager" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 			scope="request" />
<c:set var="agnHelpKey" 			value="pluginmanagerList" 		scope="request" />

<emm:ShowByPermission token="pluginmanager.change">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/administration/pluginmanager/plugin/new.action"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="pluginmanager.install"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.pluginmanager"/>
    </emm:instantiate>
</emm:instantiate>
