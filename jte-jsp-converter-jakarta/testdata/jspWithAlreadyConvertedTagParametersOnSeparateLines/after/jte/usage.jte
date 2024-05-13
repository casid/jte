@import static example.JteContext.*

<div>
    @template.tag.my.tagWithoutParameters()
    @template.tag.my.simple(
        name = "Hello " + " Hola",
        greeting = "World"
    )
    <span>
        Some text <b>@template.tag.my.simple(
                         name = firstName + lastName,
                         greeting = greetingText
                     )</b> and more text @template.tag.my.simple(
                                             name = "John",
                                             greeting = "Smith"
                                         ) and then no param tag @template.tag.my.tagWithoutParameters() here
        and even @template.tag.my.tagWithoutParameters()here
    </span>
</div>
<span>Just greeting without name: @template.tag.my.simple(
                                      greeting = "Hi"
                                  )</span>
<div>
    @template.tag.withBody(
        headline = title,
        bodyContent = @`
        <p>This is the body, for title ${title}</p>
    `
    )
</div>
<div>
    @template.tag.withBody(
        headline = title,
        bodyContent = @`<p>Body for ${title} without newlines</p>`
    )
</div>
<span>@template.tag.withBody(
          headline = title,
          bodyContent = @`Simple text`
      )</span>
