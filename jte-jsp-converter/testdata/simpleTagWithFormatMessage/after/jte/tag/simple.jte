@import static example.JteContext.*

${localize("hello.world")}

!{var hello = localize("hello.world");}
${hello}

${localize("some.params", hello, "Param 2")}

!{var params = localize("some.params", hello, "Param 2");}
${params}