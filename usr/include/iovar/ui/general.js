
function get (id)
{
    return document.getElementById(id);
}

function isStyled ()
{
    var check = document.createElement ('div');
    check.style.display = 'none';

    var body = document.getElementsByTagName ('body')[0];
    body.appendChild (check);

    var styled = true;

    if (window.getComputedStyle)
    {
        //alert ('check display: '+window.getComputedStyle (check).display);
        if (window.getComputedStyle (check).display != 'none')
        {
            styled = false;
        }
    }
    else if (check.currentStyle)
    {
        //alert ('check display: '+check.currentStyle.display);
        if (check.currentStyle.display != 'none')
        {
            styled = false;
        }
    }

    body.removeChild (check);
    return styled;
}

