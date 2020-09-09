@import static example.JteContext.*


@param String greeting
@param String name

<h2>${greeting} ${!isEmpty(name) ? ", " + name : ""}!</h2>