@import static example.JteContext.*
@import example.FoodOption

@param FoodOption foodOption
@param boolean hangover = CHOOSE_DEFAULT_VALUE

<h4>
@if(foodOption == "Pizza")
        @if(hangover)
                Pizza is <b>a good choice to recover</b>!
                Prost!
            @else
                Pizza is <b>super yummy</b>!
                That's for sure!
            @endif
    @elseif(foodOption == "Salad")
        Salad is <i>super healthy</i>!
    @else
        Enjoy your ${foodOption} :-)
    @endif

<%-- A one liner! --%>
@if(hangover)Hangover@elseNo hangover@endif
</h4>