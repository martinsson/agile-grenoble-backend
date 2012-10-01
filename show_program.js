var program = {
    "rooms" : [
        "Auditorium",
        "Makalu",
        "Kilimandjaro1",
        "Kilimandjaro3",
        "Mont blanc 1+2",
        "Mont blanc 3",
        "Mont blanc 4",
        "Cervin",
        "Evrest"
    ],
    "slots": 
    [
        {
            "all": {"title":"Accueil des participants autour d'un café"}
        },
        {
            "all": {"title":"Session Plénière: le mot des organisateurs & Sogilis"}
        },
        {
            "all": {"title":"Keynote : Reinventing software quality","speakers":["Gojko Adciz"]}
        },
        {
            "Auditorium": {"speakers":["Alain  DELAFOSSE","Nicoals  CAPPONI"," "," "],"slot":"1","title":"DevOps@Kelkoo","id":"10"},
            "Evrest": {"speakers":["Jean DUPUIS","Christophe  DUPLAIX"," "," "],"slot":"1","title":"Product Owner : comment tirer toute la puissance de l'agilité","id":"16"},
            "Cervin": {"speakers":["Rémy Sanlaville","Johan Martinsson"," "," "],"slot":"1","title":"Changer pour mieux coder","id":"26"},
            "Mont blanc 4": {"speakers":["Laurent Tardif","Sandrine Montusclat"," "," "],"slot":"1","title":"Strategie de tests","id":"31"},
            "Mont blanc 3": {"speakers":["Cyril Megard","Emmanuel Etasse"," "," "],"slot":"1","title":"C'est grave Docteur Rétro ?","id":"39"},
            "Mont blanc 1+2": {"speakers":["Jean-François Jagodzinski"," "," "," "],"slot":"1","title":"L'agilité pour les nuls","id":"65"},
            "Kilimandjaro3": {"speakers":["Thomas Ducrocq","Andréane Vuillamy"," "," "],"slot":"1","title":"comment gérer l’absence des Product Owner ?","id":"68"},
            "Kilimandjaro1": {"speakers":["Pierre Morize"," "," "," "],"slot":"1","title":"Construire une vision partagée","id":"72"},
            "Makalu": {"speakers":["Christophe NEY"," "," "," "],"slot":"1","title":"Outils informatiques agiles au sein d'une agence digitale","id":"79"}
        }
    ]
};
    
 
    
var slot_id = 0;    
$.each(program["slots"], function(islot, slot) {
    var session_html = '';
    if (slot.all) {
        // there is only one key
        $.each(slot, function (room, slot) {
            session_html += '<td colspan="9" class="plenary">'+slot['title']+'</td>';
        });
        $('#program_content').append('<tr>'+session_html+'</tr>');
    } else {
        var room_map = {};
        var room_nb = 0;
        $.each(program["rooms"], function (iroom, room) {
            room_map[room] = room_nb;
            session_html += '<td id="'+slot_id+'_'+room_nb+'"></td>';
            room_nb++;
        });
        $('#program_content').append('<tr>'+session_html+'</tr>');

        $.each(slot, function (room, session) {
            session_content = '';
            session_content += '<span class="session_title">'+session['title']+'</span> ';
            if (session['speakers']) {
                session_content += '<span class="session_speakers">'+session['speakers'].join(', ')+'</span>';
            }
            session_id = 
            $('#'+slot_id+'_'+room_map[room]).append(session_content);
        });
    }
    slot_id++;
});