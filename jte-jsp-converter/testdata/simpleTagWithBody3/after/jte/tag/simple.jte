@import static example.JteContext.*

@param String title

@template.tag.withBody(
    bodyContent = @`
    <p>This is the body, for title ${title}</p>
`
)
