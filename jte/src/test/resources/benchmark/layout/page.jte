@import org.jusecase.jte.Content
@import org.jusecase.jte.benchmark.Page

@param Page page
@param Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    @if (page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>

<body>
    <div class="menu">
        @for (org.jusecase.jte.benchmark.MenuItem menuItem : page.getMenu())
            <a href="${menuItem.url}">${menuItem.name}</a>
        @endfor
    </div>
    <div class="content">
        <h1>${page.getTitle()}</h1>
        ${content}
    </div>
    <div class="footer">
        This page has ${page.getVisits()} visits already.
    </div>
</body>

</html>