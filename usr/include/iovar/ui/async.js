function asyncLoad (url, fn)
{
    if (window.XMLHttpRequest)
    {
        var xhr = new XMLHttpRequest ();
        xhr.onreadystatechange = fn;
        xhr.open ("GET", url, true);
        xhr.send (null);
        return xhr;
    }
    else if (window.ActiveXObject)
    {
        var xhr = new ActiveXObject ("Microsoft.XMLHTTP");

        if (xhr)
        {
            xhr.onreadystatechange = fn;
            xhr.open ("GET", url, true);
            xhr.send ();
            return xhr;
        }
    }

    return false;
}

var xhrPopup = null;
function loadInPopup (link, url)
{
    xhrPopup = null;

    var overlay = false;
    var body = document.getElementsByTagName ('body')[0];
    if (body.className.indexOf (' popup')>=0) overlay = true;

    var popup;
    if (overlay) popup = get ('popup2');
    else popup = get ('popup');

    if (popup)
    {
        if (body.className.indexOf (' popup2')>=0) closePopup ();

        /*
        var loading;
        if (overlay) loading = get ('popup2_loading');
        else loading = get ('popup_loading');

        if (loading)
        {
            loading.className = '';
        }
        */

        var body = document.getElementsByTagName ('body')[0];

        if (overlay) body.className += ' popup2';
        else body.className += ' popup';

        if (! url)
        {
            url = link.href;
        }

        var ready = function ()
        {
            if (! xhrPopup)
            {
                return;
            }

            if (xhrPopup.readyState != 4)
            {
                return;
            }

            popup.className += ' ready';

            /*
            if (loading)
            {
                loading.className = 'hidden';
            }
            */

            if (xhrPopup.status == 0)
            {
                // aborted
                //alert ('aborted popup');
                return;
            }
            else if (xhrPopup.status != 200)
            {
                alert ('error: unexpected status from server: '+xhrPopup.status);
                return;
            }

            var xml = xhrPopup.responseXML;

            if (xml)
            {
                var doc = xml.documentElement;
                
                if ('HTML' === doc.nodeName.toUpperCase ())
                {
                    var body = doc.getElementsByTagName ('body')[0];

                    if (body)
                    {
                        for (var node = body.firstChild; node; node = node.nextSibling)
                        {
                            popup.appendChild (document.importNode (node, true));
                        }
                    }
                    else
                    {
                        alert ('error: html response with no body');
                        return;
                    }
                }
                else
                {
                    popup.appendChild (document.importNode (doc, true));
                }
            }
            else
            {
                var data = document.createElement ('div');
                if (xhrPopup.responseText)
                {
                    /* FIXME: very hacky */
                    if (xhrPopup.responseText.lastIndexOf ('<html', 0) === 0 || xhrPopup.responseText.lastIndexOf ('\n<html', 0) === 0)
                    {
                        /*
                        var resp = document.createElement ('div');
                        resp.innerHTML = xhrPopup.responseText;
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
                            data.innerText = '(html response with inappropriate body count: '+body.length+')';
                        }

                        resp = null;
                        */

                        var idx = xhrPopup.responseText.indexOf ('<body', 6);
                        if (idx>=0) idx = xhrPopup.responseText.indexOf ('>', idx+5);
                        if (idx >= 0)
                        {
                            var eidx = xhrPopup.responseText.indexOf ('</body>', idx+1);
                            if (eidx >= 0)
                            {
                                data.innerHTML = xhrPopup.responseText.substring (idx+1, eidx);
                            }
                            else
                            {
                                data.innerHTML = xhrPopup.responseText.substring (idx+1);
                            }
                        }
                        else
                        {
                            data.innerText = '(html response without body)';
                        }
                    }
                    else
                    {
                        data.innerText = xhrPopup.responseText;
                    }
                }
                else
                {
                    data.innerText = '(empty response)';
                }
                popup.appendChild (data);
            }

            if (! isStyled ())
            {
                if (overlay) window.location.hash = 'popup2';
                else window.location.hash = 'popup';
            }
        };

        xhrPopup = asyncLoad (url, ready);
        if (xhrPopup) return true;
        else return false;
    }
    else
    {
        //alert ('opening window');

        if (url) {
            return window.open (url,link,'width=550,height=550,location=yes,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,titlebar=yes');
        } else {
            return window.open (link.href,link.target,'width=550,height=550,location=yes,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,titlebar=yes');
        }
    }
}

function closePopup ()
{
    if (xhrPopup)
    {
        xhrPopup.abort ();
        xhrPopup = null;
    }

    var overlay = false;
    var body = document.getElementsByTagName ('body')[0];
    if (body.className.indexOf (' popup2')>=0) overlay = true;

    var popup;
    if (overlay) popup = get ('popup2');
    else popup = get ('popup');

    if (popup)
    {
        popup.className = popup.className.replace (/(?:^|\s)ready(?!\S)/g , '');

        if (overlay) body.className = body.className.replace (/(?:^|\s)popup2(?!\S)/g , '');
        else body.className = body.className.replace (/(?:^|\s)popup(?!\S)/g , '');

        if (overlay) removeData ('popup2');
        else removeData ('popup');

        if (! isStyled ())
        {
            window.location.hash = '';
        }
    }
    else
    {
        window.close ();
    }
}

function closePopupOnEscape (e)
{
    e = e || window.e;
    var c = (typeof e.which === "number") ? e.which : e.keyCode;
    if (c == 27)
    {
        closePopup ();
    }
}

var xhrInto = { };
function xhrReadyReplace (id)
{
    var xhr = xhrInto[id];
    if (! xhr) return;

    if (xhr.readyState != 4)
    {
        return;
    }

    xhrInto[id] = null;

    var loading = get (id+'_loading');
    if (loading)
    {
        loading.className = 'hidden';
    }

    var search = get (id+'_search');
    if (search)
    {
        search.className = search.className.replace (/(?:^|\s)loading(?!\S)/g , '');
    }

    if (xhr.status == 0)
    {
        // aborted
        //alert ('aborted');
        return;
    }
    else if (xhr.status != 200)
    {
        alert ('error: unexpected status from server: '+xhr.status);
        return;
    }

    var node = get (id);
    removeData (id);
    var xml = xhr.responseXML;

    if (xml)
    {
        var doc = xml.documentElement;
        
        if ('HTML' === doc.nodeName.toUpperCase ())
        {
            var body = doc.getElementsByTagName ('body')[0];

            if (body)
            {
                var data = xml.getElementById ('data');

                if (data)
                {
                    data.id = null;
                    node.appendChild (document.importNode (data, true));
                }
                else
                {
                    for (var child = body.firstChild; child; child = child.nextSibling)
                    {
                        node.appendChild (document.importNode (child, true));
                    }
                }
            }
            else
            {
                alert ('error: html response with no body');
                return;
            }
        }
        else
        {
            node.appendChild (document.importNode (doc, true));
        }
    }
    else
    {
        var data = document.createElement ('div');
        if (xhr.responseText)
        {
            data.innerText = xhr.responseText;
        }
        else
        {
            data.innerText = '(empty response)';
        }
        node.appendChild (data);
    }
}

function loadToggle (toggle, id)
{
    var xhr = xhrInto[id];
    var aborted = false;

    if (xhr)
    {
        xhr.abort ();
        xhrInto[id] = null;
        aborted = true;
    }

    if (hasData (id)) // close
    {
        removeData (id);
        return true;
    }
    else if (aborted)
    {
        return true;
    }
    else // open
    {
        var loading = get (id+'_loading');
        if (loading)
        {
            loading.className = '';
        }

        var search = get (id+'_search');
        if (search)
        {
            search.className += ' loading';
        }

        var ready = function ()
        {
            xhrReadyReplace (id);
        };

        xhrInto[id] = asyncLoad (toggle.href, ready);
        if (xhrInto[id]) return true;
        else return false;
    }
}

var lastSearch = { };
function loadSearch (e, input, id, url)
{
    var val = input.value.trim ();
    var c = (typeof e.which === "number") ? e.which : e.keyCode;
    var oldVal = lastSearch[id];

    if (c == 27) // escape key
    {
        val = '';
        input.value = val;
    }
    else if (val == oldVal && c != 13) // unchanged and not enter key
    {
        return;
    }

    lastSearch[id] = val;

    var xhr = xhrInto[id];
    if (xhr)
    {
        xhr.abort ();
        xhrInto[id] = null;
        //alert ('pre-empting existing load');
    }

    /* replacement happens server-side */
    //val = val.replace (/\\/g, '\\\\');
    //val = val.replace (/%/g, '\\%');
    //val = val.replace (/'/g, "\\'");

    var loading = get (id+'_loading');
    var search = get (id+'_search');

    if (val == '' && c != 13) // blank and not enter (enter on blank will trigger load instead)
    {
        if (loading)
        {
            loading.className = 'hidden';
        }

        if (search)
        {
            search.className = search.className.replace (/(?:^|\s)loading(?!\S)/g , '');
        }

        removeData (id);
    }
    else
    {
        if (loading)
        {
            loading.className = '';
        }

        if (search)
        {
            search.className += ' loading';
        }

        //removeData (id);

        var ready = function ()
        {
            xhrReadyReplace (id);
        };

        //alert ('about to load: '+url+'?query='+encodeURIComponent (val));
        //window.location.href = url+'?query='+encodeURIComponent (val);

        xhrInto[id] = asyncLoad (url+'?query='+encodeURIComponent (val), ready);
        if (xhrInto[id]) return true;
        else return false;
    }
}

function hasData (id)
{
    var node = get (id);
    var data = false;

    var child = node.firstChild;
    while (child)
    {
        var next = child.nextSibling;

        if (! (child.id && child.id.indexOf (id+'_') == 0))
        {
            data = true;
            break;
        }

        child = next;
    }

    return data;
}

function removeData (id)
{
    var node = get (id);

    var child = node.firstChild;
    while (child)
    {
        var next = child.nextSibling;

        /* check for child.id.indexOf for the case where a form has an element named id */
        if (! (child.id && child.id.indexOf && child.id.indexOf (id+'_') == 0))
        {
            node.removeChild (child);
        }

        child = next;
    }
}

