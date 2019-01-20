/*
 * Copyright (C) 2016 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */

function ajaxLoad (url, fn)
{
    if (window.XMLHttpRequest)
    {
        var ajax = new XMLHttpRequest ();
        ajax.onreadystatechange = fn;
        ajax.open ("GET", url, true);
        ajax.send (null);
        return ajax;
    }
    else if (window.ActiveXObject)
    {
        var ajax = new ActiveXObject ("Microsoft.XMLHTTP");

        if (ajax)
        {
            ajax.onreadystatechange = fn;
            ajax.open ("GET", url, true);
            ajax.send ();
            return ajax;
        }
    }

    return false;
}

function ajaxLoadAll (nodes, fn)
{
    if (window.XMLHttpRequest)
    {
        var ajaxes = [];
        Array.prototype.forEach.call (nodes, function (node) {
            var ajax = new XMLHttpRequest ();
            if (ajax)
            {
                ajax.onreadystatechange = fn (ajax, node);
                ajax.open ("GET", node.getAttribute("href"), true);
                ajax.send (null);
            }
        });
        return ajaxes;
    }
    else if (window.ActiveXObject)
    {
        var ajaxes = [];
        Array.prototype.forEach.call (nodes, function (node) {
            var ajax = new ActiveXObject ("Microsoft.XMLHTTP");

            if (ajax)
            {
                ajax.onreadystatechange = fn (ajax, node);
                ajax.open ("GET", node.getAttribute("href"), true);
                ajax.send ();
            }
        });
        return ajaxes;
    }

    return false;
}

