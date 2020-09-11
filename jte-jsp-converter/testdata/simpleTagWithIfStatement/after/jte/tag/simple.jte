@import static example.JteContext.*

@param String greeting
@param String name

<h2>${greeting}
@if(!isEmpty(name))
    , ${name}
@endif
!</h2>