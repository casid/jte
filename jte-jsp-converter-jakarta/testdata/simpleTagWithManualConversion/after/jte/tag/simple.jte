@import static example.JteContext.*

@param String greeting
@param String name
<h2>${greeting}, <fmt:formatDate value="name" pattern="yyyy"></fmt:formatDate>!</h2>
