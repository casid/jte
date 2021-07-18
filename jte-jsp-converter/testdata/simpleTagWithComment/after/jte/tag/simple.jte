@import static example.JteContext.*

<%-- comment1 --%>
@if(someCondition)
    some text
<%-- comment2 --%>
@elseif(anotherCondition)
    <%-- comment3 --%>
    @if(someCondition)
        some text
    <%-- comment4 --%>
    @elseif(anotherCondition)
        some other text
    @endif
@endif