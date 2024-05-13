@import static example.JteContext.*
@import gg.jte.support.ForSupport
@import java.util.List

@param List rows

<div class="well content m-b-3">
    @for(var loop : ForSupport.of(rows))
        !{var row = loop.get();}
        <strong>${row.label}</strong>
        <strong>${loop.first}</strong>
        <strong>${loop.last}</strong>
        <strong>${loop.index}</strong>
    @endfor
</div>