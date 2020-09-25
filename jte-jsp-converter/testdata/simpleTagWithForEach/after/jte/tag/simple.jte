@import static example.JteContext.*

@param java.util.List rows

<div class="well content m-b-3">
    @for(var row : rows)
        <div class="row m-b-2">
            <div class="col-sm-12 col-md-8">
                <p class="m-y-1">
                    <strong>${row.label}</strong>
                </p>
            </div>
        </div>
    @endfor
</div>