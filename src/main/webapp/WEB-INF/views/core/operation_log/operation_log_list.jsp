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
	$("#pagedTable").tableHighlight();
	$("#sortHead").headSort();
	<shiro:hasPermission name="core:operation_log:view">
	$("#pagedTable tbody tr").dblclick(function(eventObj) {
		var nodeName = eventObj.target.nodeName.toLowerCase();
		if(nodeName!="input"&&nodeName!="select"&&nodeName!="textarea") {
			//location.href=$("#view_opt_"+$(this).attr("beanid")).attr('href');
		}
	});
	</shiro:hasPermission>
});
function confirmDelete() {
	return confirm("<s:message code='confirmDelete'/>");
}
function optSingle(opt) {
	if(Cms.checkeds("ids")==0) {
		alert("<s:message code='pleaseSelectRecord'/>");
		return false;
	}
	if(Cms.checkeds("ids")>1) {
		alert("<s:message code='pleaseSelectOne'/>");
		return false;
	}
	var id = $("input[name='ids']:checkbox:checked").val();
	location.href=$(opt+id).attr("href");
}
function optMulti(form, action, msg) {
	if(Cms.checkeds("ids")==0) {
		alert("<s:message code='pleaseSelectRecord'/>");
		return false;
	}
	if(msg && !confirm(msg)) {
		return false;
	}
	form.action=action;
	form.submit();
	return true;
}
function optDelete(form) {
	if(Cms.checkeds("ids")==0) {
		alert("<s:message code='pleaseSelectRecord'/>");
		return false;
	}
	if(!confirmDelete()) {
		return false;
	}
	form.action='delete.do';
	form.submit();
	return true;
}
</script>
</head>
<body class="c-body">
<jsp:include page="/WEB-INF/views/commons/show_message.jsp"/>
<div class="c-bar margin-top5">
  <span class="c-position"><s:message code="operationLog.management"/> - <s:message code="list"/></span>
	<span class="c-total">(<s:message code="totalElements" arguments="${pagedList.totalElements}"/>)</span>
</div>
<form action="list.do" method="get">
	<fieldset class="c-fieldset">
    <legend><s:message code="search"/></legend>
    <label class="c-lab"><s:message code="operationLog.name"/>: <input type="text" name="search_LIKE_name" value="${search_LIKE_name[0]}" style="width:100px;"/><span class="in-prompt" title="<s:message code='operationLog.name.prompt'/>">&nbsp;</span></label>
    <label class="c-lab"><s:message code="operationLog.user"/>: <input type="text" name="search_LIKE_user.username" value="${requestScope['search_LIKE_user.username'][0]}" style="width:100px;"/></label>
    <label class="c-lab"><s:message code="operationLog.description"/>: <input type="text" name="search_CONTAIN_description" value="${search_CONTAIN_description[0]}" style="width:100px;"/></label>
    <label class="c-lab"><s:message code="operationLog.dataId"/>: <input type="text" name="search_EQ_dataId" value="${search_EQ_dataId[0]}" style="width:50px;"/></label>
    <label class="c-lab"><s:message code="operationLog.ip"/>: <input type="text" name="search_LIKE_ip" value="${search_LIKE_ip[0]}" style="width:100px;"/></label>
    <label class="c-lab"><s:message code="beginTime"/>: <f:text name="search_GTE_time_Date" value="${search_GTE_time_Date[0]}" onclick="WdatePicker({dateFmt:'yyyy-MM-ddTHH:mm:ss'});" style="width:120px;"/></label>
    <label class="c-lab"><s:message code="endTime"/>: <f:text name="search_LTE_time_Date" value="${search_LTE_time_Date[0]}" onclick="WdatePicker({dateFmt:'yyyy-MM-ddTHH:mm:ss'});" style="width:120px;"/></label>
	  <label class="c-lab"><input type="submit" value="<s:message code="search"/>"/></label>
  </fieldset>
</form>
<form method="post">
<tags:search_params/>
<div class="ls-bc-opt">
	<shiro:hasPermission name="core:operation_log:delete">
	<div class="ls-btn"><input type="button" value="<s:message code="delete"/>" onclick="return optDelete(this.form);"/></div>
	</shiro:hasPermission>
	<div style="clear:both"></div>
</div>
<table id="pagedTable" border="0" cellpadding="0" cellspacing="0" class="ls-tb margin-top5">
  <thead id="sortHead" pagesort="<c:out value='${page_sort[0]}' />" pagedir="${page_sort_dir[0]}" pageurl="list.do?page_sort={0}&page_sort_dir={1}&${searchstringnosort}">
  <tr class="ls_table_th">
    <th width="25"><input type="checkbox" onclick="Cms.check('ids',this.checked);"/></th>
    <th width="50"><s:message code="operate"/></th>
    <th width="30" class="ls-th-sort"><span class="ls-sort" pagesort="id">ID</span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="name"><s:message code="operationLog.name"/></span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="dataId"><s:message code="operationLog.description"/>(<s:message code="operationLog.dataId"/>)</span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="time"><s:message code="operationLog.time"/>(<s:message code="operationLog.ip"/>)</span></th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="bean" varStatus="status" items="${pagedList.content}">
  <tr beanid="${bean.id}">
    <td><input type="checkbox" name="ids" value="${bean.id}"/></td>
    <td align="center">
      <%-- 
    	<shiro:hasPermission name="core:operation_log:view">
      <a id="view_opt_${bean.id}" href="view.do?id=${bean.id}&position=${pagedList.number*pagedList.size+status.index}&${searchstring}" class="ls-opt"><s:message code="view"/></a>
      </shiro:hasPermission>
       --%>
    	<shiro:hasPermission name="core:operation_log:delete">
      <a href="delete.do?ids=${bean.id}&${searchstring}" onclick="return confirmDelete();" class="ls-opt"><s:message code="delete"/></a>
      </shiro:hasPermission>
     </td>
    <td><c:out value="${bean.id}"/></td>
    <td>
      <div><s:message code="${bean.name}" text="${bean.name}"/>(<c:out value="${bean.name}"/>)</div>
      <div style="color:blue;"><c:out value="${bean.user.username}"/></div>
    </td>
    <td>
      <div><c:out value="${fnx:substringx_sis(bean.description,35,'...')}"/></div>
      <c:if test="${!empty bean.dataId}"><div>ID:<span style="color:blue;"><c:out value="${bean.dataId}"/></span></div></c:if>
    </td>
    <td align="center">
      <div><fmt:formatDate value="${bean.time}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
      <div style="color:blue;"><c:out value="${bean.ip}"/></div>
    </td>
  </tr>
  </c:forEach>
  </tbody>
</table>
<c:if test="${fn:length(pagedList.content) le 0}"> 
<div class="ls-norecord margin-top5"><s:message code="recordNotFound"/></div>
</c:if>
</form>
<form action="list.do" method="get" class="ls-page">
	<tags:search_params excludePage="true"/>
  <tags:pagination pagedList="${pagedList}"/>
</form>
</body>
</html>