<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<div id="hello"><h:form id="log">
			<h1><h:outputText
				value="#{msg.headerHello} #{sessionScope.LoginBean.username}" />! <h:commandLink
				action="#{LoginBean.logout}" title="#{msg.headerLogout}">
				<h:outputText value=" #{msg.headerLogout}" />
			</h:commandLink></h1>
		</h:form></div>

		<h:form id="download" rendered="#{TreeBean.directory}">
			<h:outputText value="#{msg.downloadInfo}" />
			<h:commandLink action="createList"
				actionListener="#{TreeBean.createList}" title="#{msg.download}"
				value="#{msg.download}">
				<br />
				<h:message styleClass="errorText" id="downloadError" for="download" />
			</h:commandLink>

			<br />
			<br />

		</h:form>

		<h:outputLink value="./uploadFile.jsf">
			<h:graphicImage url="/images/add.png" />
			<f:verbatim> ${msg.uploadFile}</f:verbatim>
		</h:outputLink>

		<br />

		<h:form id="clientTree" rendered="#{TreeBean.directory}">

			<h2>List of Files:</h2>

			<h:message styleClass="errorText" id="clientTreeError"
				for="clientTree" />

			<t:tree2 id="clientTree" value="#{TreeBean.treeData}" var="node"
				varNodeToggler="t" showRootNode="false" binding="#{TreeBean.tree}">
				<f:facet name="person">
					<h:panelGroup>
						<f:facet name="expand">
							<t:graphicImage value="images/person.png"
								rendered="#{t.nodeExpanded}" border="0" />
						</f:facet>
						<f:facet name="collapse">
							<t:graphicImage value="images/person.png"
								rendered="#{!t.nodeExpanded}" border="0" />
						</f:facet>
						<h:outputText value="#{node.description}" styleClass="nodeFolder" />
						<h:outputText value=" (#{node.childCount})"
							styleClass="childCount" rendered="#{!empty node.children}" />
					</h:panelGroup>
				</f:facet>
				<f:facet name="list-folder">
					<h:panelGroup>
						<f:facet name="expand">
							<t:graphicImage value="images/yellow-folder-open.png"
								rendered="#{t.nodeExpanded}" border="0" />
						</f:facet>
						<f:facet name="collapse">
							<t:graphicImage value="images/yellow-folder-closed.png"
								rendered="#{!t.nodeExpanded}" border="0" />
						</f:facet>
						<h:outputText value="#{node.description}" styleClass="nodeFolder" />
						<h:outputText value=" (#{node.childCount})"
							styleClass="childCount" rendered="#{!empty node.children}" />
					</h:panelGroup>
				</f:facet>
				<f:facet name="document">
					<h:panelGroup>
						<h:commandLink immediate="true"
							styleClass="#{t.nodeSelected ? 'documentSelected':'document'}"
							actionListener="#{t.setNodeSelected}"
							action="#{TreeBean.selectedNode}">
							<t:graphicImage value="images/document.png" border="0" />
							<h:outputText value="#{node.description}" />
							<f:param name="docNum" value="#{node.identifier}" />
						</h:commandLink>
					</h:panelGroup>
				</f:facet>

			</t:tree2>
		</h:form>

	</f:view>
</c:if>
