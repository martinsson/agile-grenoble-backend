var program = 
[
    [
	{"slot":"1","title":"Challenge Kanban","id":"2"},
	{"slot":"1","title":"Duo de retour d’expérience sauce aigre douce ","id":"3"},
	{"slot":"1","title":"Des mots, des maux ? Démo !","id":"7"},
	{"slot":"1","title":"Don Quichotte et Sancho testa","id":"8"},
	{"slot":"1","title":"DevOps@Kelkoo","id":"10"},
	{"slot":"1","title":"Sky Castle Game","id":"15"},
	{"slot":"1","title":"Product Owner : comment tirer toute la puissance de l'agilité","id":"16"},
	{"slot":"1","title":"Retour d’expérience : Agile contre Cycle en V : Le match","id":"17"},
	{"slot":"1","title":"Il était une fois la vie d’un Product Owner","id":"18"}
    ],
    [
        {"slot":"2","title":"Scrum à Kanban : 3 retours d’expériences","id":"21"},
        {"slot":"2","title":"Les tests et l'agilité : la vraie vie","id":"24"},
        {"slot":"2","title":"Changer pour mieux coder","id":"26"},
        {"slot":"2","title":"Différences entre approches agiles","id":"28"},
        {"slot":"2","title":"Qualité: Stop à la procrastination","id":"29"},
        {"slot":"2","title":"La fleur de lotus ou comment redynamiser vos rétrospectives","id":"30"},
        {"slot":"2","title":"La fleur de rose ou comment redynamiser vos rétrospectives","id":"31"},
        {"slot":"2","title":"La fleur de yuka ou comment redynamiser vos rétrospectives","id":"32"},
        {"slot":"2","title":"La fleur de sel ou comment redynamiser vos rétrospectives","id":"33"}
    ]
];
$.each(program, function(islot, slot) {
    var session_html = '';
    $.each(slot, function (isession, session) {
        session_html += '<div class="span1">'+session['title']+'</div>';
    });
    $('#slots').append('<div class="row room">'+session_html+'</div>');
});
/*
    <div class="row room">
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
        <div class="span1">...</div>
    </div>
*/