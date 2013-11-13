
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

var theme_colors = {
    "Management": "th_management",
    "Kanban-Lean": "th_kanban",
    "Découverte": "th_decouverte",
    "Portfolio": "th_portfolio",
    "Scrum master / Coaching": "th_coaching",
    "Technique": "th_technique"}

	
var personas_colors = {
    "Eric, Explorateur Agile": "eric",
    "Mathieu, Manager Produit": "mathieu",
    "Dimitri, Développeur Agile": "dimitri",
    "Patrick, Programmeur": "patrick",
    "Alain, Architecte Logiciel": "alain",
    "Tiana, Testeur/QA": "tiana",
	"Carole, Chef de Projet": "carole",
	"Stéphane, Scrum Master": "stephane",
	"Philippe, Program Manager": "philippe",
	"Claude, Champion (ou coach interne)": "claude",
	"Christophe, Consultant": "christophe",
	"Adrien, Analyste Métier/Fonctionnel": "adrien",
	"Daphné, Designer UI/Ergonome": "daphne",
	"Denis, Dirigeant d'entreprise (ou Manager R&D)": "denis"
	}

var personasFieldNameCounter = 'data-personas-compteur';


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
		create_toolbarPersonas();
        format_program(p);
    }
}
);

function create_toolbarPersonas() {
	createCheckboxPersonas();
	createClickEventOnPersonas();
}

function createCheckboxPersonas() {
	for (name in personas_colors) {
	  if (personas_colors.hasOwnProperty(name)) {
		$('#toolbar_personas').append('<input id="checkbox_' + name + '" class="css-checkbox" type="checkbox" data-personas="' + personas_colors[name] + '" />');
		$('#toolbar_personas').append('<label for="checkbox_' + name + '" name="checkbox_lbl_' + name + '" class="css-label">' + name + '</label>');
	  }
	}
	$('.css-checkbox').prop("checked", true);
}

function createClickEventOnPersonas() {
	$('.css-checkbox').click(function() {
		var $checkbox = $(this);
		var $personasName = $checkbox.attr('data-personas');
		if(isChecked($checkbox)) {
			displayCheckedPersonas($personasName);
		}
		else {
			dealWithUncheckedPersonas($personasName);
		}
	});
}

function displayCheckedPersonas($personasName) {
	$('.' + $personasName).each(function() {
		var $sessionItem = $(this);
		displaySessionColor($sessionItem);
		incrementPersonasCounter($sessionItem);
	});
}

function dealWithUncheckedPersonas($personasName) {
	$('.' + $personasName).each(function() {
		var $sessionItem = $(this);
		decrementPersonasCounter($sessionItem);
		if(isPersonasCounterZero($sessionItem)) {
			hideSessionColor($sessionItem);
		}
	});
}

function isChecked($checkbox) {
	return $checkbox.prop("checked");
}

function displaySessionColor($item) {
	$item.addClass($item.attr('data-session-theme')).removeClass('th_unselected');
}

function hideSessionColor($item) {
	$item.removeClass($item.attr('data-session-theme')).addClass('th_unselected');
}

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
            insert_session_span(slot_id, room, 'rowspan', rowspan);
            var colspan = parseInt(session['width'] || 1);
           	insert_session_span(slot_id, room, 'colspan', colspan);
			var currentItem = slot_id+'_'+room_map[room].id;
			var $item = $('#'+currentItem);
            $item.append(format_session(session));
            $item.attr('class', theme_colors[session.theme]  + ' rounded');
			
			fillPersonasInfo($item, session.theme);
			
			// Uncomment to test example
			//fillPersonasExample($item, currentItem, session.theme);
        });
    }
}

function fillPersonasInfo($item, theme) {
	$item.attr('data-session-theme', theme_colors[theme]);
	createPersonasCounter($item);
	// TODO: add class for each personas in this current session
}

function fillPersonasExample($item, currentItem, theme) {
	if(currentItem == "3_0") {
		addOnePersonasInfo($item, personas_colors["Mathieu, Manager Produit"]);
	}else if(currentItem == "3_1") {
		addOnePersonasInfo($item, personas_colors["Eric, Explorateur Agile"]);
		addOnePersonasInfo($item, personas_colors["Mathieu, Manager Produit"]);
	}
	else if(currentItem == "3_2") {
		addOnePersonasInfo($item, personas_colors["Eric, Explorateur Agile"]);
		addOnePersonasInfo($item, personas_colors["Dimitri, Développeur Agile"]);
	}
	else if(currentItem == "3_3") {
		addOnePersonasInfo($item, personas_colors["Eric, Explorateur Agile"]);
		addOnePersonasInfo($item, personas_colors["Dimitri, Développeur Agile"]);
		addOnePersonasInfo($item, personas_colors["Mathieu, Manager Produit"]);
	}
	else if(currentItem == "3_4") {
		addOnePersonasInfo($item, personas_colors["Dimitri, Développeur Agile"]);
	}
	else {
		$item.removeClass(theme_colors[theme]);
		$item.attr('class', 'unselected'  + ' rounded');
		$item.attr('data-session-theme', 'unselected');
	}
}

function addOnePersonasInfo($item, namePersonas) {
	$item.addClass(namePersonas);
	incrementPersonasCounter($item);
}

function createPersonasCounter($item) {
	$item.attr(personasFieldNameCounter, '0');
}

function incrementPersonasCounter($item) {
	$item.attr(personasFieldNameCounter, parseInt($item.attr(personasFieldNameCounter))+1);
}

function decrementPersonasCounter($item) {
	$item.attr(personasFieldNameCounter, parseInt($item.attr(personasFieldNameCounter))-1);
}

function isPersonasCounterZero($item) {
	return (parseInt($item.attr(personasFieldNameCounter)) === 0);
}

function insert_session_span(slot_id, room, attribute, value) {
	$('#'+slot_id+'_'+room_map[room].id).attr(attribute, value);
	times(value - 1, function(n) {
		var selector = '#'+(parseInt(slot_id)+n+1)+'_'+room_map[room].id;
		removal.push(selector);
	});
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
	session_url = '<a target="_parent" href="http://2013.agile-grenoble.org/keynote">'+session_title+'</a>';
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
    session_html += ' <a href="#program_head" class="back_to_top">Retour au PROgramme</a>';
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
        session_html += '<br/>' //wtf get rid of this test
    })
    return session_html;
}
