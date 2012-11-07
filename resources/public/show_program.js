
var room_map = {
    "Auditorium":{"id": 0, "capacity" : 530},
    "Makalu": {"id":1, "capacity" : 110},  
    "Kilimanjaro 1": {"id":2, "capacity" : 55},
    "Kilimanjaro 3": {"id":3, "capacity" : 55},
    "Mont Blanc 3": {"id":4, "capacity" : 24},
    "Mont Blanc 1+2": {"id":5, "capacity" : 55},
    "Mont Blanc 4": {"id":6, "capacity" : 24},
    "Cervin": {"id":7, "capacity" : 40},
    "Everest": {"id":8, "capacity" : 40}
};

var slot_hours = [
    "8h00",
    "8h30",
    "9h00",
    "10h00",
    "10h50",
    "11h10",
    "12h00",
    "13h35",
    "13h45",
    "14h50",
    "15h40",
    "16h10",
    "17h00",
    "17h20",
    "18h30",
    "20h30"
];

$.ajax({
    url:'json/program-summary-with-roomlist',
    success: function (p) {
        format_program(p);
    }
}
);

function format_program(program) {
    var slot_id = 0;
    var previous_was_plenary = false;
    $.each(program["slots"], function(islot, slot) {
        if (!slot.all && !previous_was_plenary) {
            $('#program_content').append(change_room(slot_id));
            slot_id++;
        }

        var session_html = get_hour_cell(slot_id);
        format_slot(slot_id, slot, session_html);
        
        previous_was_plenary = false;
        if (slot.all) {
            previous_was_plenary = true;
        }
        slot_id++;
    });
    
    $.each(program["sessions"], function(session_id, session) {
        if (session_id) {
            $('#program_detail').append('<div>'+format_session_detail(session)+'</div>');
        }
    });
}

function format_slot(slot_id, slot, session_html) {
    if (slot.all) {
        format_plenary(session_html, slot);
    } else {
        prefill_empty_cells(session_html, slot_id);

        $.each(slot, function (room, session) {
            $('#'+slot_id+'_'+room_map[room].id).append(format_session(session));
        });
    }
}

function get_rooms_slot() {
    var session_html = '<td> </td>';
    $.each(room_map, function (room_name, room) {
        session_html += '<td class="room_desc">'+room_name+'<br />('+room.capacity+' places)</td>';
    });
    return '<tr>'+session_html+'</tr>';
}

function format_plenary(session_html, slot) {
    var it_was_keynote = false;
    // there is only one key
    $.each(slot, function (room, session) {
        if (session.type == 'keynote') {
            it_was_keynote = true;
        }
        session_html += '<td colspan="9" class="plenary '+session.type+'">'+format_session(session, it_was_keynote)+'</td>';
    });
    $('#program_content').append('<tr>'+session_html+'</tr>');

    if (it_was_keynote) {
        $('#program_content').append(get_rooms_slot());
    }
}

function change_room(slot_id) {
    var session_html = get_hour_cell(slot_id);
    session_html += '<td colspan="9" class="plenary change_room">Changement de salle</td>';
    return '<tr>'+session_html+'</tr>';
}

function get_hour_cell(slot_id) {
    return '<td class="hour">'+slot_hours[slot_id]+'</td>';
}

function prefill_empty_cells(session_html, slot_id) {
    $.each(room_map, function (room_name, room) {
        session_html += '<td id="'+slot_id+'_'+room.id+'">&nbsp;</td>';
    });
    $('#program_content').append('<tr>'+session_html+'</tr>');    
}

function format_session(session, keynote) {
    var session_title = '<span class="session_title">'+session['title']+'</span> ';
    var session_url   = session_title;
    if (keynote) {
	session_url = '<a href="http://2012.agile-grenoble.org/keynote">'+session_title+'</a>';
    }
    if (session.id) {
        session_url = '<a href="#session_detail_'+session['id']+'">'+session_title+'</a>';
    }
    
    session_content = session_url;
    if (session['speakers']) {
        session_content += '<span class="session_speakers">'+session['speakers'].join(', ')+'</span>';
    }
    
    return session_content;
}

function format_session_detail(session) {
    var session_html = '<h2>';
    session_html += '<a id="session_detail_'+session['id']+'" name="session_detail_'+session['id']+'">'+session['title']+'</a>';
    session_html += '</h2>';
    session_html += ' <a href="#program_head" class="back_to_top">Retour au programme</a>';
    slides = session.slides;
    if (session.slides) {
    	session_html += '<p class="slides"><a href="'+session.slides+'">support / slides</a></p>'
    }
    session_html += '<p class="logistic">'+session['room']+' - '+session['format']+'</p>';
    session_html += '<p class="abstract"><b>Résumé : </b>'+session['abstract']+'</p>';
    session_html += '<p class="abstract"><b>Bénéfices pour les participants : </b>'+session['benefits']+'</p>';
    $.each(session['speaker-list'], function (id, speaker) {
        session_html += '<p class="speaker">'
        session_html += '<span class="speaker_name">'+[speaker['firstname'], speaker['lastname']].join(', ')+' </span>';
        if 	(speaker['bio'] != "")	session_html += ' : '+speaker['bio']+''
        session_html += '</p>'
        session_html += '<br/>' //wtf get rid of this
    })
    return session_html;
}