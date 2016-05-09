<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fnx" uri="http://java.sun.com/jsp/jstl/functionsx"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://www.jspxcms.com/tags/form"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>Jspxcms管理平台 - Powered by Jspxcms</title>
<jsp:include page="/WEB-INF/views/commons/head.jsp"></jsp:include>
<script type="text/javascript">
$(function() {
	$("#validForm").validate();
	$("input[name='name']").focus();
	$("input[name='cycle']").click(cycleOnClick);
	cycleOnClick();
});
function cycleOnClick() {
	var cycle = $("input[name=cycle]:checked").val();
	if(cycle=="1") {
	  $("#cronCycle input[type=text]").prop("disabled",false).removeClass("disabled");
	  $("#simpleCycle input[type=text],#simpleCycle select").prop("disabled",true).addClass("disabled");		
	} else {
    $("#cronCycle input[type=text]").prop("disabled",true).addClass("disabled");
    $("#simpleCycle input[type=text],#simpleCycle select").prop("disabled",false).removeClass("disabled");  		
	}
}
function confirmDelete() {
	return confirm("<s:message code='confirmDelete'/>");
}
</script>
</head>
<body class="c-body">
<jsp:include page="/WEB-INF/views/commons/show_message.jsp"/>
<div class="c-bar margin-top5">
  <span class="c-position"><s:message code="scheduleJob.management"/> - <s:message code="${oprt=='edit' ? 'edit' : 'create'}"/></span>
</div>
<form id="validForm" action="${oprt=='edit' ? 'update' : 'save'}.do" method="post">
<tags:search_params/>
<f:hidden name="oid" value="${bean.id}"/>
<f:hidden name="position" value="${position}"/>
<f:hidden name="data_siteId" value="${site.id }"/>
<f:hidden name="data_siteName" value="${site.name}"/>
<f:hidden name="data_userId" value="${user.id}"/>
<input type="hidden" id="redirect" name="redirect" value="edit"/>
<table border="0" cellpadding="0" cellspacing="0" class="in-tb margin-top5">
  <tr>
    <td colspan="4" class="in-opt">
			<shiro:hasPermission name="core:schedule_job:create">
			<div class="in-btn"><input type="button" value="<s:message code="create"/>" onclick="location.href='create.do?${searchstring}';"<c:if test="${oprt=='create'}"> disabled="disabled"</c:if>/></div>
			<div class="in-btn"></div>
			</shiro:hasPermission>
			<shiro:hasPermission name="core:schedule_job:copy">
			<div class="in-btn"><input type="button" value="<s:message code="copy"/>" onclick="location.href='create.do?id=${bean.id}&${searchstring}';"<c:if test="${oprt=='create'}"> disabled="disabled"</c:if>/></div>
			</shiro:hasPermission>
			<shiro:hasPermission name="core:schedule_job:delete">
			<div class="in-btn"><input type="button" value="<s:message code="delete"/>" onclick="if(confirmDelete()){location.href='delete.do?ids=${bean.id}&${searchstring}';}"<c:if test="${oprt=='create'}"> disabled="disabled"</c:if>/></div>
			</shiro:hasPermission>
			<div class="in-btn"></div>
			<div class="in-btn"><input type="button" value="<s:message code="prev"/>" onclick="location.href='edit.do?id=${side.prev.id}&position=${position-1}&${searchstring}';"<c:if test="${empty side.prev}"> disabled="disabled"</c:if>/></div>
			<div class="in-btn"><input type="button" value="<s:message code="next"/>" onclick="location.href='edit.do?id=${side.next.id}&position=${position+1}&${searchstring}';"<c:if test="${empty side.next}"> disabled="disabled"</c:if>/></div>
			<div class="in-btn"></div>
			<div class="in-btn"><input type="button" value="<s:message code="return"/>" onclick="location.href='list.do?${searchstring}';"/></div>
      <div style="clear:both;"></div>
    </td>
  </tr>
  <tr>
  <td class="in-lab" width="15%"><em class="required">*</em><s:message code="scheduleJob.code"/>:</td>
  <td class="in-ctt" width="85%" colspan="3">
	  <c:choose>
	  <c:when test="${oprt == 'create'}">
	    <select name="code" onchange="location.href='create.do?code='+$(this).val();">
	      <c:forEach var="c" items="${codes}">        
	      <f:option value="${c}" selected="${code}"><s:message code="scheduleJob.code.${c}"/></f:option>
	      </c:forEach>
	    </select>
	  </c:when>
	  <c:otherwise>
	    <s:message code="scheduleJob.code.${bean.code}"/>
	    <f:hidden name="code" value="${bean.code}"/>
	  </c:otherwise>
	  </c:choose>
  </td>
  </tr>
  <tr>
    <td class="in-lab" width="15%"><em class="required">*</em><s:message code="scheduleJob.name"/>:</td>
    <td class="in-ctt" width="35%"><f:text name="name" value="${oprt=='edit' ? (bean.name) : ''}" class="required" maxlength="100" style="width:180px;"/></td>
    <td class="in-lab" width="15%"><em class="required">*</em><s:message code="scheduleJob.status"/>:</td>
    <td class="in-ctt" width="35%">
      <label><f:radio name="status" value="0" checked="${bean.status}" default="0"/><s:message code="scheduleJob.status.0"/></label>
      <label><f:radio name="status" value="1" checked="${bean.status}"/><s:message code="scheduleJob.status.1"/></label>
    </td>
  </tr>
  <tr>    
    <td class="in-lab" width="15%"><s:message code="scheduleJob.description"/>:</td>
    <td class="in-ctt" width="85%" colspan="3">
      <f:text name="description" value="${bean.description}" style="width:500px;"/>
    </td>
  </tr>
  <c:if test="${oprt == 'edit'}">
  <tr>    
	  <td class="in-lab" width="15%"><s:message code="scheduleJob.data"/>:</td>
	  <td class="in-ctt" width="85%" colspan="3">
	    <f:text value="${bean.data}" style="width:500px;" readonly="readonly" disabled="disabled"/>
	  </td>
  </tr>
  </c:if>
  <c:if test="${!empty includePage}">
    <jsp:include page="${includePage}" />
  </c:if>
  <tr>
    <td class="in-lab" width="15%"><s:message code="scheduleJob.startTime"/>:</td>
    <td class="in-ctt" width="35%">
      <input type="text" name="startTime" value="<fmt:formatDate value="${bean.startTime}" pattern="yyyy-MM-dd'T'HH:mm:ss"/>" onclick="WdatePicker({dateFmt:'yyyy-MM-ddTHH:mm:ss'});" style="width:180px;"/>
    </td>
    <td class="in-lab" width="15%"><s:message code="scheduleJob.endTime"/>:</td>
    <td class="in-ctt" width="35%">
      <input type="text" name="endTime" value="<fmt:formatDate value="${bean.endTime}" pattern="yyyy-MM-dd'T'HH:mm:ss"/>" onclick="WdatePicker({dateFmt:'yyyy-MM-ddTHH:mm:ss'});" style="width:180px;"/>
    </td>
  </tr>
  <tr>
    <td class="in-lab" width="15%"><em class="required">*</em><s:message code="scheduleJob.cycle"/>:</td>
    <td class="in-ctt" width="85%" colspan="3">
      <div id="simpleCycle">
        <label><f:radio name="cycle" value="2" checked="${bean.cycle}"/></label>
        <s:message code="scheduleJob.startDelay"/>: <f:text name="startDelay" value="${bean.startDelay}" class="digits" style="width:120px;"/><s:message code="scheduleJob.unit.3"/> &nbsp;
        <s:message code="scheduleJob.repeatInterval"/>: <f:text name="repeatInterval" value="${bean.repeatInterval}" class="required digits" style="width:120px;"/>
        <select name="unit">
          <f:option value="1" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.1"/></f:option>
          <f:option value="2" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.2"/></f:option>
          <f:option value="3" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.3"/></f:option>
          <f:option value="4" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.4"/></f:option>
          <f:option value="5" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.5"/></f:option>
          <f:option value="6" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.6"/></f:option>
          <f:option value="7" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.7"/></f:option>
          <f:option value="8" selected="${bean.unit}" default="3"><s:message code="scheduleJob.unit.8"/></f:option>
        </select>        
      </div>
      <div id="cronCycle" style="margin-top:3px;">
        <label><f:radio name="cycle" value="1" checked="${bean.cycle}" default="1"/><s:message code="scheduleJob.cronExpression"/>:</label> &nbsp;
        <f:text name="cronExpression" value="${bean.cronExpression}" class="required" style="width:180px"/>
      </div>
    </td>
  </tr>
  <tr>
    <td colspan="4" class="in-opt">
      <div class="in-btn"><input type="submit" value="<s:message code="save"/>"/></div>
      <div class="in-btn"><input type="submit" value="<s:message code="saveAndReturn"/>" onclick="$('#redirect').val('list');"/></div>
      <c:if test="${oprt=='create'}">
      <div class="in-btn"><input type="submit" value="<s:message code="saveAndCreate"/>" onclick="$('#redirect').val('create');"/></div>
      </c:if>
      <div style="clear:both;"></div>
    </td>
  </tr>
</table>
</form>
</body>
</html>