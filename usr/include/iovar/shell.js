/*
 * Copyright (C) 2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */

function exec_submit(form) {
    var p = document.getElementById('prompt');
    p.className = 'busy';
}

function exec_load(frame) {
    var p = document.getElementById('prompt');
    if (p.className != 'begin') {
        p.className = '';
    }
}

