<%@ taglib prefix="my" uri="/WEB-INF/tld/my.tld" %>

<%@ attribute name="title" required="true" type="java.lang.String" %>

<my:jte jte="tag/withBody.jte" headline="${title}">
    <p>This is the body, for title ${title}</p>
</my:jte>