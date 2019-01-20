/*
 * Copyright (C) 2016 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 *
 * depends on: common.js, ajax.js
 */

function turnme (node1, node2, node3)
{
    node1.className += ' rotated';
    node2.className += ' rotated';

    loadFrame (node3, 'http://localhost:8080/app/work/open?=9', true);
}

function turnem (node1, node2, node3)
{
    node1.className = node1.className.replace (/(?:^|\s)rotated(?!\S)/g, '');
    node2.className = node2.className.replace (/(?:^|\s)rotated(?!\S)/g, '');
}


var _frames = new Map (); // keyed by frame node

/*
 * arguments:
 *      frame   - node to load content into
 *      url     - url to load
 *      replace - if true, replace's any existing contents in target node
 *      callback - optional callback function after successful frame load
 */
function loadFrame (frame, url, replace, callback)
{
    frame.className = frame.className.replace (/(?:^|\s)ready(?!\S)/g, '');
    var ajax = ajaxLoad (url, _frameReady (frame, replace, callback));
    if (ajax)
    {
        _frames.set (frame, ajax);
    }
}

/*
 * returns reference to call-back function for ajax state changes
 */
function _frameReady (frame, replace, callback)
{
    return function ()
    {
        //alert ('yo: ' + frame);

        var ajax = _frames.get (frame);

        if (! ajax)
        {
            return;
        }

        if (ajax.readyState != 4)
        {
            return;
        }

        if (replace)
        {
            while (frame.firstChild)
            {
                frame.removeChild (frame.firstChild);
            }
        }

        frame.className += ' ready';

        if (ajax.status == 0)
        {
            // aborted?
            return;
        }
        else if (ajax.status != 200)
        {
            frame.innerHTML = 'frame: unexpected status: '+ajax.status;
            return;
        }

        var xml = ajax.responseXML;
        if (xml)
        {
            var doc = xml.documentElement;
            
            if ('HTML' === doc.nodeName.toUpperCase ())
            {
                var body = xml.getElementById ('body');
                if (! body)
                {
                    body = doc.getElementsByTagName ('body')[0];
                }

                if (body)
                {
                    for (var node = body.firstChild; node; node = node.nextSibling)
                    {
                        frame.appendChild (document.importNode (node, true));
                    }
                }
                else
                {
                    frame.innerHTML = 'frame: html response with no body';
                    return;
                }
            }
            else
            {
                frame.appendChild (document.importNode (doc, true));
            }
        }
        else
        {
            var data = document.createElement ('div');
            if (ajax.responseText)
            {
                /* FIXME: very hacky */
                if (ajax.responseText.lastIndexOf ('<html', 0) === 0 || ajax.responseText.lastIndexOf ('\n<html', 0) === 0)
                {
                    /*
                    var resp = document.createElement ('div');
                    resp.innerHTML = ajax.responseText;
                    var body = resp.getElementsByTagName ('body');

                    if (body.length === 1)
                    {
                        for (var node = body[0].firstChild; node; node = node.nextSibling)
                        {
                            alert ('append child: '+node);
                            data.appendChild (node);
                        }
                    }
                    else
                    {
                        data.innerHTML = '(html response with inappropriate body count: '+body.length+')';
                    }

                    resp = null;
                    */

                    var idx = ajax.responseText.indexOf ('<body', 6);
                    if (idx>=0) idx = ajax.responseText.indexOf ('>', idx+5);
                    if (idx >= 0)
                    {
                        var eidx = ajax.responseText.indexOf ('</body>', idx+1);
                        if (eidx >= 0)
                        {
                            data.innerHTML = ajax.responseText.substring (idx+1, eidx);
                        }
                        else
                        {
                            data.innerHTML = ajax.responseText.substring (idx+1);
                        }
                    }
                    else
                    {
                        data.innerHTML = '(html response without body)';
                        return;
                    }
                }
                else
                {
                    data.innerHTML = htmlSafe (ajax.responseText);
                }
            }
            else
            {
                data.innerHTML = '(empty response)';
                return;
            }

            frame.appendChild (data);
        }

        if (callback)
        {
            callback();
        }
    };
}

