@import static example.JteContext.*
@import gg.jte.Content

@param Content bodyContent
@param String greeting
@param String name

<h2>${greeting} ${!isEmpty(name) ? ", " + name : ""}!</h2>
<p>${bodyContent}</p>