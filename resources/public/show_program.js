
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


var personasFieldNameCounter = 'data-personas-compteur';
var personasLegendId = 'personasLegend';
var personasToolbarId = 'personasToolbar';
var classHideListPersonas = 'hidePersonas';
var thSessionUnselected = 'th_unselected';
var dataSessionTheme = 'data-session-theme';
var dataPersonas = 'data-personas';
var classPersonasUncheckedImage = 'grayscale';
var classPersonasUncheckedTextColor = 'graycolor';
var classPersonasImageClickable = 'css-checkbox';

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
    url:'json/personas',
    success: function (p) {
       create_toolbarPersonas(p);
    }
}
);

$.ajax({
    url:'json/program-summary-with-roomlist',
    success: function (p) {
        format_program(p);
    }
}
);

function create_toolbarPersonas(personas) {
	createCheckboxPersonas(personas);
	createClickEventOnPersonas();
    createClickEventOnLegend();
}

function createCheckboxPersonas(personas) {
    var $personasToolbarHead = $('#' + personasToolbarId);
	for (name in personas) {
    
        var $checkboxItem = createPersonasStructure(name, personas[name].photo, personas[name].intitule);
        $personasToolbarHead.append($checkboxItem);
	}
}

function transformToLowercaseWithoutSpecialChar(s) {
  s = s.toLowerCase();
  var translate = {
    "ä": "a", "ö": "o", "ü": "u", "é": "e",
    "Ä": "A", "Ö": "O", "Ü": "U"  
  };
  var translate_re = /[öäüÖÄÜé]/g;
  return ( s.replace(translate_re, function(match) { 
    return translate[match]; 
  }) );
}

function createPersonasStructure(name, photo, intitule) {
    var $checkboxItem = $('<li ' + getZonePersonasClasses() + getImageDataPersonas(name) + '>');
    $checkboxItem.append('<img ' + getImageId(name) + getImageClasses() + getImageSrc(photo) + getImageSize() + '></img>');
    $checkboxItem.append('<label for="' + getPersonasId(name) + '">' + intitule + '</label>');
    return $checkboxItem;
}

function getZonePersonasClasses() {
    return ' class="' + classPersonasImageClickable + '" ';
}

function getImageId(name) {
    return ' id="' + getPersonasId(name) + '" ';
}

function getPersonasId(name) {
    return 'img_' + name;
}

function getImageClasses() {
    return ' class="rounded" ';
}

function getImageDataPersonas(name) {
    return ' ' + dataPersonas + '="' + name + '" ';
}

function getImageSrc(photo) {
    return ' src=' + photo + ' ';
}

function getImageSize() {
    return ' width="30" height="36" ';
}

function createClickEventOnPersonas() {
	$('.' + classPersonasImageClickable).click(function() {
        var $personas = $(this);
        if(areAllPersonasSelected($personas)) {
            keepOnlyCurrentPersonasAsSelected($personas);
        }
        else {
            dealWithCurrentPersonasOnly($personas);
        }
	});
}

function createClickEventOnLegend() {
    $('#' + personasLegendId).click(function() {
        $('#' + personasToolbarId).slideToggle();
        updatePersonasLegendText($(this));
    });
}

function areAllPersonasSelected($personas) {
    return ($('#' + personasToolbarId).find('.' + classPersonasUncheckedTextColor).length == 0)
}

function keepOnlyCurrentPersonasAsSelected($currentPersonas) {
    var currentDataPersonas = getDataPersonas($currentPersonas);
    $('.' + classPersonasImageClickable).each(function () {
        var $personas = $(this);
        if(getDataPersonas($personas) != currentDataPersonas) {
            dealWithCurrentPersonasOnly($personas);
        }
    });
}

function dealWithCurrentPersonasOnly($personas) {
    updatePersonasColorText($personas);
    updatePersonasColorImage($personas);
    
    var $personasName = getDataPersonas($personas);
    if(isChecked($personas)) {
        displayCheckedPersonas($personasName);
    }
    else {
        dealWithUncheckedPersonas($personasName);
    }
}

function getDataPersonas($personas) {
    return $personas.attr(dataPersonas);
}

function updatePersonasColorText($personas) {
    $personas.toggleClass(classPersonasUncheckedTextColor);
}

function updatePersonasColorImage($personas) {
    $personas.find('img').toggleClass(classPersonasUncheckedImage);
}

function updatePersonasLegendText($legend) {
    $legend.toggleClass(classHideListPersonas);
    if($legend.hasClass(classHideListPersonas)) {
        $legend.text('Personas (Cliquez pour afficher la liste des personas)');
    }
    else {
        $legend.text('Personas (Cliquez sur l\'image pour sélectionner un personas)');
    }
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

function isChecked($personas) {
	return !$personas.hasClass(classPersonasUncheckedTextColor);
}

function displaySessionColor($item) {
	$item.addClass($item.attr(dataSessionTheme), 500).removeClass(thSessionUnselected, 500);
}

function hideSessionColor($item) {
	$item.removeClass($item.attr(dataSessionTheme), 500).addClass(thSessionUnselected, 500);
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
        $('#program_content').find('[data-session-id=' + session.id + ']').each(function() {
            fillPersonasInfo($(this), session.personas);
        });
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
			$item.attr(dataSessionTheme, theme_colors[session.theme]);
            $item.attr('data-session-id', session.id);
        });
    }
}

function fillPersonasInfo($item, personas) {
	createPersonasCounter($item);
    
    for (name in personas) {
        if(personas[name] != "") {
            addOnePersonasInfo($item, transformToLowercaseWithoutSpecialChar(personas[name]));
        }
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
