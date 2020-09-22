@import static example.JteContext.*



${localize("hello.world")}

!{var hello = localize("hello.world");}
${hello}

${localize("some.params", hello, "Param 2")}

!{var params = localize("some.params", hello, "Param 2");}
${params}

@if(5 > 0)
    <span class="text-md">
        ${localize("nested", localize("common.x", "Something"))}
    </span>
@endif
