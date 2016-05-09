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
	<shiro:hasPermission name="core:publish_point:edit">
	$("#pagedTable tbody tr").dblclick(function(eventObj) {
		var nodeName = eventObj.target.nodeName.toLowerCase();
		if(nodeName!="input"&&nodeName!="select"&&nodeName!="textarea") {
			location.href=$("#edit_opt_"+$(this).attr("beanid")).attr('href');
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
  <span class="c-position"><s:message code="publishPoint.management"/> - <s:message code="list"/></span>
	<span class="c-total">(<s:message code="totalElements" arguments="${fn:length(list)}"/>)</span>
</div>
<form action="list.do" method="get">
	<fieldset class="c-fieldset">
    <legend><s:message code="search"/></legend>
	  <label class="c-lab"><s:message code="publishPoint.name"/>: <input type="text" name="search_CONTAIN_name" value="${search_CONTAIN_name[0]}" style="width:150px;"/></label>
	  <label class="c-lab"><input type="submit" value="<s:message code="search"/>"/></label>
  </fieldset>
</form>
<form method="post">
<tags:search_params/>
<div class="ls-bc-opt">
	<shiro:hasPermission name="core:publish_point:create">
	<div class="ls-btn"><input type="button" value="<s:message code="create"/>" onclick="location.href='create.do?${searchstring}';"/></div>
	<div class="ls-btn"></div>
	</shiro:hasPermission>
	<shiro:hasPermission name="core:publish_point:copy">
	<div class="ls-btn"><input type="button" value="<s:message code="copy"/>" onclick="return optSingle('#copy_opt_');"/></div>
	</shiro:hasPermission>
	<shiro:hasPermission name="core:publish_point:edit">
	<div class="ls-btn"><input type="button" value="<s:message code="edit"/>" onclick="return optSingle('#edit_opt_');"/></div>
	</shiro:hasPermission>
	<shiro:hasPermission name="core:publish_point:delete">
	<div class="ls-btn"><input type="button" value="<s:message code="delete"/>" onclick="return optDelete(this.form);"/></div>
	</shiro:hasPermission>
	<div style="clear:both"></div>
</div>
<table id="pagedTable" border="0" cellpadding="0" cellspacing="0" class="ls-tb margin-top5">
  <thead id="sortHead" pagesort="<c:out value='${page_sort[0]}' />" pagedir="${page_sort_dir[0]}" pageurl="list.do?page_sort={0}&page_sort_dir={1}&${searchstringnosort}">
  <tr class="ls_table_th">
    <th width="25"><input type="checkbox" onclick="Cms.check('ids',this.checked);"/></th>
    <th width="110"><s:message code="operate"/></th>
    <th width="30" class="ls-th-sort"><span class="ls-sort" pagesort="id">ID</span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="name"><s:message code="publishPoint.name"/></span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="storePath"><s:message code="publishPoint.storePath"/></span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="displayPath"><s:message code="publishPoint.displayPath"/></span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="method"><s:message code="publishPoint.method"/></span></th>
    <th class="ls-th-sort"><span class="ls-sort" pagesort="type"><s:message code="publishPoint.type"/></span></th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="bean" varStatus="status" items="${list}">
  <tr beanid="${bean.id}">
    <td><input type="checkbox" name="ids" value="${bean.id}"/></td>
    <td align="center">
    	<shiro:hasPermission name="core:publish_point:copy">
      <a id="copy_opt_${bean.id}" href="create.do?id=${bean.id}&${searchstring}" class="ls-opt"><s:message code="copy"/></a>
      </shiro:hasPermission>
    	<shiro:hasPermission name="core:publish_point:edit">
      <a id="edit_opt_${bean.id}" href="edit.do?id=${bean.id}&position=${status.index}&${searchstring}" class="ls-opt"><s:message code="edit"/></a>
      </shiro:hasPermission>
    	<shiro:hasPermission name="core:publish_point:delete">
      <a href="delete.do?ids=${bean.id}&${searchstring}" onclick="return confirmDelete();" class="ls-opt"><s:message code="delete"/></a>
      </shiro:hasPermission>
     </td>
    <td><c:out value="${bean.id}"/></td>
    <td><c:out value="${bean.name}"/></td>
    <td><c:if test="${bean.method == 3}">${bean.ftpHostname}</c:if><c:out value="${bean.storePath}"/></td>
    <td><c:out value="${bean.displayPath}"/></td>
    <td><s:message code="publishPoint.method.${bean.method}"/></td>
    <td><s:message code="publishPoint.type.${bean.type}"/></td>
  </tr>
  </c:forEach>
  </tbody>
</table>
<c:if test="${fn:length(list) le 0}"> 
<div class="ls-norecord margin-top5"><s:message code="recordNotFound"/></div>
</c:if>
</form>
</body>
</html>