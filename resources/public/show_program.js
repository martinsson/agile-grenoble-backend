
var room_map = {
    "Auditorium":{"id": 0, "capacity" : 530},
    "Makalu": {"id":1, "capacity" : 110},  
    "Kili 1+2": {"id":2, "capacity" : 55},
    "Kili 3+4": {"id":3, "capacity" : 55},
    "Cervin": {"id":4, "capacity" : 40},
    "Everest": {"id":5, "capacity" : 40},
    "Mt-Blanc 1": {"id":6, "capacity" : 24},
    "Mt-Blanc 2": {"id":7, "capacity" : 24},
    "Mt-Blanc 3": {"id":8, "capacity" : 24},
    "Mt-Blanc 4": {"id":9, "capacity" : 24},
};

var slot_hours = [
    "8h00",
    "8h30",
    "9h00",
    "10h00",
    "(10h15)",
    "(10h30)",
    "10h45",
    "11h05",
    "(11h20)",
    "(11h35)",
    "11h50",
    "13h20",
    "13h45",
    "14h30",
    "14h50",
    "(15h05)",
    "(15h20)",
    "15h35",
    "16h05",
    "(16h20)",
    "(16h35)",
    "16h50",
    "17h10",
    "(17h25)",
    "(17h40)",
    "17h55",
    "18h15",
];

function times(n, callback) {
  for(var i=0; i<n; i++) {
    callback(i);
  }
}

function deepCopy(object) {
  return $.extend(true, {}, object);
}

var removal = [];

$.ajax({
    url:'json/program-summary-with-roomlist',
    success: function (p) {
        // FIXTURES START //////////////////
        // $.each([11, 10, 8, 4, 3], function(_, i) {
        //   times(2, function() {
        //     p.slots.splice(i, 0, deepCopy(p.slots[i]));
        //   });
        // });
        // p.slots[4]['Makalu']['length'] = '2';
        // delete(p.slots[5]['Makalu']);
        // p.slots[6]['Cervin']['length'] = '3';
        // delete(p.slots[7]['Cervin']);
        // delete(p.slots[8]['Cervin']);
        // p.slots[12]['Auditorium']['length'] = '2';
        // delete(p.slots[13]['Auditorium']);
        // p.slots[19]['Everest']['length'] = '3';
        // delete(p.slots[20]['Everest']);
        // delete(p.slots[21]['Everest']);
        // FIXTURES END ////////////////////
        format_program(p);
    }
}
);

function format_program(program) {
    var slot_id = 0;
    var previous_non_plenary_slots = 0;
    $.each(program["slots"], function(islot, slot) {
        var session_html = get_hour_cell(slot_id);
        format_slot(slot_id, slot, session_html);
        slot_id++;
    });
    $.each(removal, function(_, selector) {
      $(selector).remove();
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
            var rowspan = parseInt(session['length'] || 1);
            if(rowspan > 1) {
                $('#'+slot_id+'_'+room_map[room].id).attr('rowspan', rowspan);
                times(rowspan - 1, function(n) {
                    var selector = '#'+(parseInt(slot_id)+n+1)+'_'+room_map[room].id;
                    removal.push(selector);
                });
            }
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
        session_html += '<td colspan="10" class="plenary '+session.type+'">'+format_session(session, it_was_keynote)+'</td>';
    });
    $('#program_content').append('<tr>'+session_html+'</tr>');

    if (it_was_keynote) {
        $('#program_content').append(get_rooms_slot());
    }
}

function change_room(slot_id) {
    var session_html = get_hour_cell(slot_id);
    session_html += '<td colspan="10" class="plenary change_room">Changement de salle</td>';
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
	session_url = '<a href="http://2013.agile-grenoble.org/keynote">'+session_title+'</a>';
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
    if (slides) {
    	session_html += '<p class="slides"><a href="'+slides+'">support / slides</a></p>'
    }
    session_html += '<p class="logistic">'+session['room']+'</p>';
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
