@import static example.JteContext.*

!{var hello = "Hello World!";}
${hello}

!{var content = @`
    @if(true)
        <p>Hello World</p>
    @else
        <p>Hello, false World</p>
    @endif
`;}
${content}