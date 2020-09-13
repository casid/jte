@import static example.JteContext.*

@param String greeting
@param String name

<h2>${greeting}
<my:simple-dependency/>
${!isEmpty(name) ? ", " + name : ""}!</h2>

