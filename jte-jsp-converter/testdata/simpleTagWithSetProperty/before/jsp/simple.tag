<%@ attribute name="bean" required="true" type="example.SimpleBean" %>
<jsp:setProperty name="bean" property="stringProperty" value="hello" />
${bean.stringProperty}