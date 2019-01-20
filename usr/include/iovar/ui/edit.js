
function confirmDelete (link)
{
    var async = get ('async');
    if (async)
    {
        link.target = 'async';
    }

    return confirm ("About to delete. Are you sure?");
}

var submits = [ ];
var submitting = false;
var attachedEvent = false;
submits.push = function (form)
{
    var async = get ('async');
    if (async)
    {
        form.target = 'async';
    }
    else
    {
        if (form.target == 'async')
        {
            form.target = '';
        }

        return false;
    }

    var found = false;
    for (var i=0; i<submits.length; i++)
    {
        if (submits[i]==form)
        {
            found = true;
            break;
        }
    }

    if (!found && arguments[0] instanceof HTMLFormElement)
    {
        //alert ('adding: '+form);
        Array.prototype.push.apply (this, [ form ]);

        if (!submitting)
        {
            submitting = true;
            submitNext ();
        }
    }

    return true;
};

function submitNext ()
{
    //alert ('here we go');

    var form = submits.shift ();
    
    if (form)
    {
        //alert (form);
        submitting = true;
        //alert ('got one: '+form);

        if (!attachedEvent)
        {
            attachedEvent = true;

            var async = get ('async');
            if (async)
            {
                if (async.addEventListener)
                {
                    async.addEventListener ('load', function () { submitNext (); });
                }
                else
                {
                    async.attachEvent ('onload', function () { submitNext (); });
                }
            }
        }

        form.submit ();
    }
    else
    {
        submitting = false;
    }
}

function chksame (check, num)
{
    var len = check.form.elements.length;
    for (var i=0; i<len; i++)
    {
        var elem = check.form.elements[i];
        if (elem.type=='textarea' || elem.type=='text' || elem.type=='button')
        {
            elem.disabled = check.checked;
        }
    }

    //check.form.submit();
    submits.push (check.form);
}

